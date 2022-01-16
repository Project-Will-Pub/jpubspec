package xyz.rk0cc.willpub.pubspec.parser;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.*;
import xyz.rk0cc.jogu.GitRepositoryURL;
import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.pubspec.data.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public final class PubspecParser {
    public static final ObjectMapper PUBSPEC_MAPPER;

    public static final Set<String> PUBSPEC_YAML_FIELD = Set.of(
            "name",
            "environment",
            "version",
            "description",
            "publish_to",
            "homepage",
            "repository",
            "issue_tracker",
            "documentation",
            "dependencies",
            "dev_dependencies",
            "dependency_overrides"
    );

    static {
        final YAMLFactory yaml = new YAMLFactory()
                .enable(Feature.MINIMIZE_QUOTES)
                .enable(Feature.LITERAL_BLOCK_STYLE)
                .enable(Feature.INDENT_ARRAYS)
                .enable(Feature.INDENT_ARRAYS_WITH_INDICATOR)
                .disable(Feature.USE_NATIVE_TYPE_ID)
                .disable(Feature.CANONICAL_OUTPUT)
                .disable(Feature.WRITE_DOC_START_MARKER);

        final SimpleModule pubspecMod = new SimpleModule();
        pubspecMod.addSerializer(Pubspec.class, new PubspecToYAML());
        pubspecMod.addDeserializer(Pubspec.class, new PubspecFromYAML());

        PUBSPEC_MAPPER = new ObjectMapper(yaml).registerModule(pubspecMod);
    }

    private static final class PubspecFromYAML extends StdDeserializer<Pubspec> {
        public PubspecFromYAML() {
            this(null);
        }

        public PubspecFromYAML(Class<?> vc) {
            super(vc);
        }

        @Nonnull
        @Override
        public Pubspec deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
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

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();

                drs.add(DependencyReferenceDictionary.detectReference(entry.getValue()).jsonToRef(entry));
            }
        }

        @Nonnull
        private static LinkedHashMap<String, Object> jsonNodeAFParser(@Nonnull ObjectNode node) {
            ObjectNode dcn = node.deepCopy();

            dcn.remove(PUBSPEC_YAML_FIELD);

            return new LinkedHashMap<>(PUBSPEC_MAPPER.convertValue(dcn, new TypeReference<Map<String, Object>>(){}));
        }
    }

    private static final class PubspecToYAML extends StdSerializer<Pubspec> {
        public PubspecToYAML() {
            this(null);
        }

        public PubspecToYAML(Class<Pubspec> t) {
            super(t);
        }

        @Override
        public void serialize(
                @Nonnull Pubspec pubspec,
                JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider
        ) throws IOException {
            // Take a snapshot to prevent modification from provider
            PubspecSnapshot snapshot = PubspecSnapshot.getSnapshotOfCurrentPubspec(pubspec);
            PubSemVerConstraint sdk = snapshot.environment().sdk();

            assert snapshot.additionalData().keySet().stream().noneMatch(PUBSPEC_YAML_FIELD::contains);

            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("name", snapshot.name());

            if (snapshot.description() != null)
                jsonGenerator.writeStringField("description", snapshot.description());

            if (snapshot.version() != null)
                jsonGenerator.writeStringField("version", snapshot.version().value());

            // Env object wrapper
            jsonGenerator.writeObjectFieldStart("environment");
            jsonGenerator.writeStringField("sdk", sdk.rawConstraint());

            if (snapshot.environment().flutter() != null)
                jsonGenerator.writeStringField("flutter", snapshot.environment().flutter().rawConstraint());

            jsonGenerator.writeEndObject();
            // End env

            if (snapshot.homepage() != null)
                jsonGenerator.writeStringField("homepage", snapshot.homepage().toString());

            if (snapshot.repository() != null)
                jsonGenerator.writeStringField("repository", snapshot.repository().toString());

            if (snapshot.issueTracker() != null)
                jsonGenerator.writeStringField("issue_tracker", snapshot.issueTracker().toString());

            if (snapshot.documentation() != null)
                jsonGenerator.writeStringField("documentation", snapshot.documentation().toString());

            if (snapshot.dependencies().size() > 0) {
                jsonGenerator.writeObjectFieldStart("dependencies");
                writeDRSInJson(snapshot.dependencies(), jsonGenerator, sdk);
                jsonGenerator.writeEndObject();
            }

            if (snapshot.devDependencies().size() > 0) {
                jsonGenerator.writeObjectFieldStart("dev_dependencies");
                writeDRSInJson(snapshot.devDependencies(), jsonGenerator, sdk);
                jsonGenerator.writeEndObject();
            }

            if (snapshot.dependencyOverrides().size() > 0) {
                jsonGenerator.writeObjectFieldStart("dependency_overrides");
                writeDRSInJson(snapshot.dependencyOverrides(), jsonGenerator, sdk);
                jsonGenerator.writeEndObject();
            }

            // Append remaining additional field
            jsonGenerator.writeTree(PUBSPEC_MAPPER.valueToTree(snapshot.additionalData()));

            jsonGenerator.writeEndObject();
        }

        private static void writeDRSInJson(
                @Nonnull DependenciesReferenceSet drs,
                @Nonnull JsonGenerator jg,
                @Nonnull PubSemVerConstraint sdk
        ) throws IOException {
            assert drs.size() > 0;

            for (DependencyReference dr : drs)
                DependencyReferenceDictionary.detectReference(dr).refToJson(dr, jg, sdk);
        }
    }
}

interface DependencyDefinition<D extends DependencyReference> {
    boolean relatedJsonStructure(@Nonnull JsonNode node);

    boolean isCorrespondedType(@Nonnull DependencyReference ref);

    @Nonnull
    D jsonToDR(@Nonnull String name, @Nonnull JsonNode node) throws Exception;

    void drToJson(
            @Nonnull JsonGenerator dependencyJsonNode,
            @Nonnull DependencyReference ref,
            @Nonnull PubSemVerConstraint sdkVC
    ) throws IOException;
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
                @Nonnull JsonGenerator dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) throws IOException {
            String vc = ((HostedReference) ref).versionConstraint().rawConstraint();

            if (vc == null) dependencyJsonNode.writeNullField(ref.name());
            else dependencyJsonNode.writeStringField(ref.name(), vc);
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
            return new LocalReference(name, Paths.get(node.get("path").textValue()));
        }

        @Override
        public void drToJson(
                @Nonnull JsonGenerator dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) throws IOException {
            String path = ((LocalReference) ref).path().toString();

            dependencyJsonNode.writeObjectFieldStart(ref.name());
            dependencyJsonNode.writeStringField("path", path);
            dependencyJsonNode.writeEndObject();
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
                @Nonnull JsonGenerator dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) throws IOException {
            GitReference gref = (GitReference) ref;
            String url = gref.repositoryURL().assembleURL(),
                    path = gref.path(),
                    gitref = gref.ref();

            dependencyJsonNode.writeObjectFieldStart(ref.name());
            if (path == null && gitref == null)
                dependencyJsonNode.writeStringField("git", url);
            else {
                dependencyJsonNode.writeObjectFieldStart("git");
                dependencyJsonNode.writeStringField("url", url);

                if (path != null)
                    dependencyJsonNode.writeStringField("path", path);

                if (gitref != null)
                    dependencyJsonNode.writeStringField("ref", gitref);

                dependencyJsonNode.writeEndObject();
            }
            dependencyJsonNode.writeEndObject();
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
                @Nonnull JsonGenerator dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) throws IOException {
            ThirdPartyHostedReference tphref = (ThirdPartyHostedReference) ref;

            final boolean useSuccinct = PubspecParsePreference.eligible(
                    PubspecParsePreference.SUCCINCT_THIRD_PARTY_HOSTED_FORMAT,
                    sdkVC
            ) && tphref.name().equals(tphref.hostedName());

            dependencyJsonNode.writeObjectFieldStart(ref.name());

            if (useSuccinct)
                dependencyJsonNode.writeStringField("hosted", tphref.hostedName());
            else {
                dependencyJsonNode.writeObjectFieldStart("hosted");
                dependencyJsonNode.writeStringField("name", tphref.hostedName());
                dependencyJsonNode.writeStringField("url", tphref.repositoryURL().toString());
                dependencyJsonNode.writeEndObject();
            }

            if (tphref.versionConstraint().rawConstraint() != null)
                dependencyJsonNode.writeStringField("version", tphref.versionConstraint().rawConstraint());

            dependencyJsonNode.writeEndObject();
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
                @Nonnull JsonGenerator dependencyJsonNode,
                @Nonnull DependencyReference ref,
                @Nonnull PubSemVerConstraint sdkVC
        ) throws IOException {
            SDKReference sref = (SDKReference) ref;

            dependencyJsonNode.writeObjectFieldStart(ref.name());
            dependencyJsonNode.writeStringField("sdk", sref.sdk());

            if (sref.versionConstraint().rawConstraint() != null)
                dependencyJsonNode.writeStringField("version", sref.versionConstraint().rawConstraint());

            dependencyJsonNode.writeEndObject();
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

    public void refToJson(
            @Nonnull DependencyReference ref,
            @Nonnull JsonGenerator jsonWriter,
            @Nonnull PubSemVerConstraint sdk
    ) throws IOException {
        definition.drToJson(jsonWriter, ref, sdk);
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
}
