package xyz.rk0cc.willpub.pubspec;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.*;
import xyz.rk0cc.jogu.GitRepositoryURL;
import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.josev.SemVerRangeNode;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.pubspec.data.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public final class PubspecParser {
    public static final ObjectMapper PUBSPEC_MAPPER;

    public static final Set<String> PUBSPEC_YAML_FIELD = Set.of();

    static {
        final YAMLFactory yaml = new YAMLFactory()
                .enable(Feature.MINIMIZE_QUOTES)
                .enable(Feature.LITERAL_BLOCK_STYLE)
                .enable(Feature.INDENT_ARRAYS)
                .enable(Feature.INDENT_ARRAYS_WITH_INDICATOR)
                .disable(Feature.USE_NATIVE_TYPE_ID)
                .disable(Feature.CANONICAL_OUTPUT)
                .disable(Feature.WRITE_DOC_START_MARKER);
        PUBSPEC_MAPPER = new ObjectMapper(yaml);
    }



    public static final class PubspecFromYAML extends StdDeserializer<Pubspec> {
        public PubspecFromYAML() {
            this(null);
        }

        public PubspecFromYAML(Class<?> vc) {
            super(vc);
        }

        @Override
        public Pubspec deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JacksonException {
            try {
                ObjectNode pubspecYAML = jsonParser.getCodec().readTree(jsonParser);
                final String name = pubspecYAML.get("name").textValue();

                ObjectNode pubspecEnv = (ObjectNode) pubspecYAML.get("environment");
                final PubspecEnvironment environment = new PubspecEnvironment(
                        PubSemVerConstraint.parse(pubspecEnv.get("sdk").textValue()),
                        pubspecEnv.has("flutter")
                                ? PubSemVerConstraint.parse(pubspecEnv.get("flutter").textValue())
                                : null
                );

                final SemVer version = SemVer.parse(pubspecYAML.get("version").textValue());

                final String description = pubspecYAML.has("description")
                        ? pubspecYAML.get("description").textValue()
                        : null;

                final String publishTo = pubspecYAML.has("publish_to")
                        ? pubspecYAML.get("publish_to").textValue()
                        : null;

                final URL homepage = pubspecYAML.has("homepage")
                                ? new URL(pubspecYAML.get("homepage").textValue())
                                : null,
                        repository = pubspecYAML.has("repository")
                                ? new URL(pubspecYAML.get("repository").textValue())
                                : null,
                        issueTracker = pubspecYAML.has("issue_tracker")
                                ? new URL(pubspecYAML.get("issue_tracker").textValue())
                                : null,
                        documentation = pubspecYAML.has("documentation")
                                ? new URL(pubspecYAML.get("documentation").textValue())
                                : null;

                final ImportedReferenceSet dependencies = new ImportedReferenceSet(),
                        devDependencies = new ImportedReferenceSet();

                final OverrideReferenceSet dependencyOverrides = new OverrideReferenceSet();

                JsonNode depNode = pubspecYAML.get("dependencies"),
                         devDepNode = pubspecYAML.get("devDependencies"),
                         depOverrideNode = pubspecYAML.get("dependencyOverrides");

                if (depNode.isObject())
                    assignDRFromNode((ObjectNode) depNode, dependencies);

                if (devDepNode.isObject())
                    assignDRFromNode((ObjectNode) devDepNode, devDependencies);

                if (depOverrideNode.isObject())
                    assignDRFromNode((ObjectNode) depOverrideNode, dependencyOverrides);

                return new Pubspec(
                        name,
                        environment,
                        version,
                        publishTo,
                        description,
                        homepage,
                        repository,
                        issueTracker,
                        documentation,
                        dependencies,
                        devDependencies,
                        dependencyOverrides,
                        jsonNodeAFParser(pubspecYAML)
                );
            } catch (Exception e) {
                throw new IOException("At least one exceptions throws when parsing pubspec.yaml", e);
            }
        }

        private static void assignDRFromNode(
                @Nonnull ObjectNode dependenciesNode,
                @Nonnull DependenciesReferenceSet drs
        ) throws Exception {
            final Iterator<Map.Entry<String, JsonNode>> fields = dependenciesNode.fields();

            while (fields.hasNext())
                DependencyReferenceDictionary.assignSetInOne(fields.next(), drs);
        }

        private static Map<String, Object> jsonNodeAFParser(@Nonnull ObjectNode node) {
            ObjectNode dcn = node.deepCopy();

            dcn.remove(PUBSPEC_YAML_FIELD);

            return PUBSPEC_MAPPER.convertValue(dcn, new TypeReference<>(){});
        }
    }

    public static final class PubspecToYAML extends StdSerializer<Pubspec> {

        public PubspecToYAML() {
            this(null);
        }

        public PubspecToYAML(Class<Pubspec> t) {
            super(t);
        }

        @Override
        public void serialize(Pubspec pubspec, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {

        }
    }
}

interface DependencyDefinition<D extends DependencyReference> {
    boolean relatedJsonStructure(@Nonnull JsonNode node);

    boolean isCorrespondedType(@Nonnull DependencyReference ref);

    @Nonnull
    D jsonToDR(@Nonnull String name, @Nonnull JsonNode node) throws Exception;

    void drToJson(
            @Nonnull ObjectNode dependencyJsonNode,
            @Nonnull DependencyReference ref,
            @Nonnull PubSemVerConstraint sdkVC
    );
}

enum DependencyReferenceDictionary {
    HOSTED(new DependencyDefinition<HostedReference>() {
        @Override
        public boolean relatedJsonStructure(@Nonnull JsonNode node) {
            return node.isNull() || node.isTextual();
        }

        @Override
        public boolean isCorrespondedType(@Nonnull DependencyReference ref) {
            return ref.getClass().equals(HostedReference.class);
        }

        @Nonnull
        @Override
        public HostedReference jsonToDR(@Nonnull String name, @Nonnull JsonNode node) throws Exception {
            return new HostedReference(name, PubSemVerConstraint.parse(node.textValue()));
        }

        @Override
        public void drToJson(
                @Nonnull ObjectNode dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) {
            dependencyJsonNode.put(ref.name(), ((HostedReference) ref).versionConstraint().rawConstraint());
        }
    }),
    LOCAL(new DependencyDefinition<LocalReference>() {
        @Override
        public boolean relatedJsonStructure(@Nonnull JsonNode node) {
            JsonNode path = node.get("path");
            return path != null && path.isTextual();
        }

        @Override
        public boolean isCorrespondedType(@Nonnull DependencyReference ref) {
            return ref.getClass().equals(LocalReference.class);
        }

        @Nonnull
        @Override
        public LocalReference jsonToDR(@Nonnull String name, @Nonnull JsonNode node) throws Exception {
            return new LocalReference(name, node.get("path").textValue());
        }

        @Override
        public void drToJson(
                @Nonnull ObjectNode dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) {
            ObjectNode pathNode = PubspecParser.PUBSPEC_MAPPER.createObjectNode();

            pathNode.put("path", ((LocalReference) ref).path());

            dependencyJsonNode.set(ref.name(), pathNode);
        }
    }),
    GIT(new DependencyDefinition<GitReference>() {
        @Override
        public boolean relatedJsonStructure(@Nonnull JsonNode node) {
            JsonNode git = node.get("git");

            if (git == null) return false;
            else if (git.isTextual()) return true;

            JsonNode gitURL = git.get("url");

            return gitURL != null && gitURL.isTextual();
        }

        @Override
        public boolean isCorrespondedType(@Nonnull DependencyReference ref) {
            return ref.getClass().equals(GitReference.class);
        }

        @Nonnull
        @Override
        public GitReference jsonToDR(@Nonnull String name, @Nonnull JsonNode node) throws Exception {
            JsonNode git = node.get("git");

            return git.isTextual()
                    ? new GitReference(name, GitRepositoryURL.parse(git.textValue()))
                    : new GitReference(
                            name,
                            GitRepositoryURL.parse(git.get("url").textValue()),
                            git.get("path") == null ? null : git.get("path").textValue(),
                            git.get("ref") == null ? null : git.get("ref").textValue()
                    );
        }

        @Override
        public void drToJson(
                @Nonnull ObjectNode dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) {
            ObjectNode gitNode = PubspecParser.PUBSPEC_MAPPER.createObjectNode();

            if (((GitReference) ref).path() == null && ((GitReference) ref).ref() == null)
                gitNode.put("git", ((GitReference) ref).repositoryURL().assembleURL());
            else {
                ObjectNode igitNode = PubspecParser.PUBSPEC_MAPPER.createObjectNode();

                igitNode.put("url", ((GitReference) ref).repositoryURL().assembleURL());

                if (((GitReference) ref).path() != null) igitNode.put("path", ((GitReference) ref).path());

                if (((GitReference) ref).ref() != null) igitNode.put("ref", ((GitReference) ref).ref());

                gitNode.set("git", igitNode);
            }

            dependencyJsonNode.set(ref.name(), gitNode);
        }
    }),
    THIRD_PARTY(new DependencyDefinition<ThirdPartyHostedReference>() {
        @Override
        public boolean relatedJsonStructure(@Nonnull JsonNode node) {
            JsonNode hosted = node.get("hosted");

            if (hosted == null) return false;
            else if (hosted.isTextual()) return true;

            JsonNode name = hosted.get("name"), url = hosted.get("url");

            return name != null && url != null && name.isTextual() && url.isTextual();
        }

        @Override
        public boolean isCorrespondedType(@Nonnull DependencyReference ref) {
            return ref.getClass().equals(ThirdPartyHostedReference.class);
        }

        @Nonnull
        @Override
        public ThirdPartyHostedReference jsonToDR(@Nonnull String name, @Nonnull JsonNode node) throws Exception {
            JsonNode hosted = node.get("hosted");

            if (hosted.isTextual()) return new ThirdPartyHostedReference(name, new URL(hosted.textValue()));

            return node.get("version") != null
                    ? new ThirdPartyHostedReference(
                            name,
                            new URL(hosted.get("url").textValue()),
                            hosted.get("name").textValue(),
                            PubSemVerConstraint.parse(node.get("version").textValue()))
                    : new ThirdPartyHostedReference(
                            name,
                            new URL(hosted.get("url").textValue()),
                        hosted.get("name").textValue()
                    );
        }

        @Override
        public void drToJson(
                @Nonnull ObjectNode dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) {
            SemVer minSuccinctSDK = new SemVer(2, 15);
            SemVerRangeNode sdkRN = sdkVC.start();

            final boolean useSuccinct = ThirdPartyHostedReference.SUCCINCT_THIRD_PARTY_HOSTED_FORMAT
                    && sdkRN.orEquals()
                    ? sdkRN.semVer().isGreaterOrEquals(minSuccinctSDK)
                    : sdkRN.semVer().isGreater(minSuccinctSDK);

            ObjectNode tprn = PubspecParser.PUBSPEC_MAPPER.createObjectNode();

            if (useSuccinct && ref.name().equals(((ThirdPartyHostedReference) ref).hostedName()))
                tprn.put("hosted", ((ThirdPartyHostedReference) ref).repositoryURL().toString());
            else {
                ObjectNode itprn = PubspecParser.PUBSPEC_MAPPER.createObjectNode();
                itprn.put("name", ((ThirdPartyHostedReference) ref).hostedName());
                itprn.put("url", ((ThirdPartyHostedReference) ref).repositoryURL().toString());

                tprn.set("hosted", itprn);
            }

            if (((ThirdPartyHostedReference) ref).versionConstraint().rawConstraint() != null)
                tprn.put("version", ((ThirdPartyHostedReference) ref).versionConstraint().rawConstraint());

            dependencyJsonNode.set(ref.name(), tprn);
        }
    }),
    SDK(new DependencyDefinition<SDKReference>() {
        @Override
        public boolean relatedJsonStructure(@Nonnull JsonNode node) {
            return node.get("sdk") != null && node.get("sdk").isTextual();
        }

        @Override
        public boolean isCorrespondedType(@Nonnull DependencyReference ref) {
            return ref.getClass().equals(SDKReference.class);
        }

        @Nonnull
        @Override
        public SDKReference jsonToDR(@Nonnull String name, @Nonnull JsonNode node) throws Exception {
            JsonNode ver = node.get("version");

            return ver == null
                    ? new SDKReference(name, node.get("sdk").textValue())
                    : new SDKReference(name, node.get("sdk").textValue(), PubSemVerConstraint.parse(ver.textValue()));
        }

        @Override
        public void drToJson(
                @Nonnull ObjectNode dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) {
            ObjectNode sdkNode = PubspecParser.PUBSPEC_MAPPER.createObjectNode();
            sdkNode.put("sdk", ((SDKReference) ref).sdk());

            if (((SDKReference) ref).versionConstraint().rawConstraint() != null)
                sdkNode.put("version", ((SDKReference) ref).versionConstraint().rawConstraint());

            dependencyJsonNode.set(ref.name(), sdkNode);
        }
    });

    private final DependencyDefinition<? extends DependencyReference> definition;

    DependencyReferenceDictionary(@Nonnull DependencyDefinition<? extends DependencyReference> definition) {
        this.definition = definition;
    }

    @Nonnull
    public DependencyReference jsonToRef(@Nonnull Map.Entry<String, JsonNode> jsonNodeEntry) throws Exception {
        return definition.jsonToDR(jsonNodeEntry.getKey(), jsonNodeEntry.getValue());
    }

    public void refToJson(@Nonnull DependencyReference ref, @Nonnull ObjectNode json, @Nonnull PubSemVerConstraint sdk) {
        definition.drToJson(json, ref, sdk);
    }

    @Nonnull
    static DependencyReferenceDictionary detectReference(Object detect) {
        List<DependencyReferenceDictionary> drt = Arrays.stream(DependencyReferenceDictionary.values())
                .filter(dr -> {
                    if (detect instanceof JsonNode jn) return dr.definition.relatedJsonStructure(jn);
                    else if (detect instanceof DependencyReference dro) return dr.definition.isCorrespondedType(dro);
                    return false;
                })
                .toList();

        assert drt.size() == 1;

        return drt.get(0);
    }

    @Nonnull
    static DependencyReferenceDictionary detectByMapEntry(@Nonnull Map.Entry<String, JsonNode> entry) {
        return detectReference(entry.getValue());
    }

    static void assignSetInOne(@Nonnull Map.Entry<String, JsonNode> entry, @Nonnull DependenciesReferenceSet drs)
            throws Exception {
        drs.add(detectByMapEntry(entry).jsonToRef(entry));
    }
}
