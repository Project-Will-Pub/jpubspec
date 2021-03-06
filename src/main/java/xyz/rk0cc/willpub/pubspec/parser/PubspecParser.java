package xyz.rk0cc.willpub.pubspec.parser;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import xyz.rk0cc.jogu.GitRepositoryURL;
import xyz.rk0cc.josev.NonStandardSemVerException;
import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.josev.SemVerRangeNode;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.pubspec.data.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * Handle data conversion between <code>pubspec.yaml</code> and {@link Pubspec}.
 *
 * @since 1.0.0
 */
public final class PubspecParser {
    /**
     * A {@link Set} of {@link String} that the field name is implemented already in {@link Pubspec} and do not apply
     * in {@link Pubspec#additionalData() additional data}.
     */
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
            "dependency_overrides",
            "platforms"
    );

    /**
     * A module for assigning {@link StdSerializer} and {@link StdDeserializer} for parsing {@link Pubspec}
     *
     * @return A {@link SimpleModule} of resolving data to {@link Pubspec} or vice versa.
     */
    @Nonnull
    static SimpleModule pubsepcModule() {
        final SimpleModule pubspecMod = new SimpleModule();
        pubspecMod.addSerializer(Pubspec.class, new PubspecToYAML());
        pubspecMod.addDeserializer(Pubspec.class, new PubspecFromYAML());
        return pubspecMod;
    }

    private PubspecParser() {}

    /**
     * Implemented {@link StdDeserializer} to parsing <code>pubspec.yaml</code> to {@link Pubspec} object.
     *
     * @since 1.0.0
     */
    private static final class PubspecFromYAML extends StdDeserializer<Pubspec> {
        /**
         * Construct parser without class declared.
         */
        public PubspecFromYAML() {
            this(null);
        }

        /**
         * Construct parser with class declared.
         */
        public PubspecFromYAML(Class<?> vc) {
            super(vc);
        }

        /**
         * {@inheritDoc}
         */
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

                if (depNode != null && depNode.isObject())
                    assignDRFromNode((ObjectNode) depNode, dependencies);

                if (devDepNode != null && devDepNode.isObject())
                    assignDRFromNode((ObjectNode) devDepNode, devDependencies);

                if (depOverrideNode != null && depOverrideNode.isObject())
                    assignDRFromNode((ObjectNode) depOverrideNode, dependencyOverrides);

                PubspecPlatforms platforms;

                if (pubspecYAML.has("platforms")) {
                    ObjectNode node = (ObjectNode) pubspecYAML.get("platforms");
                    platforms = new PubspecPlatforms(
                            node.has("android"),
                            node.has("ios"),
                            node.has("linux"),
                            node.has("macos"),
                            node.has("web"),
                            node.has("windows")
                    );
                } else
                    platforms = PubspecPlatforms.createAllSupported();

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
                        platforms,
                        jsonNodeAFParser(pubspecYAML)
                );
            } catch (Exception e) {
                throw new IOException("At least one exceptions throws when parsing pubspec.yaml", e);
            }
        }

        /**
         * Handling conversion between {@link ObjectNode} and {@link DependenciesReferenceSet}.
         *
         * @param dependenciesNode A node representing entire dependencies field.
         * @param drs A {@link DependenciesReferenceSet} pending to applied.
         *
         * @throws Exception Any exception thrown during applying dependencies.
         */
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

        /**
         * Apply remaining fields in <code>pubspec.yaml</code> which does not declared in {@link Pubspec}.
         *
         * @param node Entire <code>pubspec.yaml</code>'s {@link ObjectNode}.
         *
         * @return A {@link LinkedHashMap} which filtered out existed field in {@link Pubspec}.
         */
        @Nonnull
        private static LinkedHashMap<String, Object> jsonNodeAFParser(@Nonnull ObjectNode node) {
            ObjectNode dcn = node.deepCopy();

            dcn.remove(PUBSPEC_YAML_FIELD);

            return new LinkedHashMap<>(new ObjectMapper().convertValue(dcn, new TypeReference<Map<String, Object>>(){}));
        }
    }

    /**
     * Implemented {@link StdSerializer} to writing {@link Pubspec} to <code>pubspec.yaml</code>.
     *
     * @since 1.0.0
     */
    private static final class PubspecToYAML extends StdSerializer<Pubspec> {
        /**
         * Construct parser without class declared.
         */
        public PubspecToYAML() {
            this(null);
        }

        /**
         * Construct parser with class declared.
         */
        public PubspecToYAML(Class<Pubspec> t) {
            super(t);
        }

        /**
         * {@inheritDoc}
         */
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

            if (snapshot.publishTo() != null)
                jsonGenerator.writeStringField("publish_to", snapshot.publishTo());

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

            SemVerRangeNode sdkStartRange = snapshot.environment().sdk().start();
            boolean dart216;
            try {
                dart216 = sdkStartRange.orEquals()
                        ? sdkStartRange.semVer().isGreaterOrEquals(SemVer.parse("2.16.0"))
                        : sdkStartRange.semVer().isGreater(SemVer.parse("2.16.0"));
            } catch (NonStandardSemVerException e) {
                throw new RuntimeException(e);
            }

            // Display supported platform when Dart 2.16 or later
            if (dart216) {
                boolean forceShowPlatforms = PubspecParsePreference.eligible(
                        PubspecParsePreference.SHOW_PLATFORMS_ENTRY_WHEN_ALL_PLATFORM_SUPPORTED,
                        snapshot.environment().sdk()
                );

                if (
                        (forceShowPlatforms && snapshot.platforms().supportAllPlatforms())
                                || !snapshot.platforms().supportAllPlatforms()
                ) {
                    PubspecPlatforms p = snapshot.platforms();
                    jsonGenerator.writeObjectFieldStart("platforms");
                    if (p.android()) jsonGenerator.writeNullField("android");
                    if (p.ios()) jsonGenerator.writeNullField("ios");
                    if (p.linux()) jsonGenerator.writeNullField("linux");
                    if (p.macos()) jsonGenerator.writeNullField("macos");
                    if (p.web()) jsonGenerator.writeNullField("web");
                    if (p.windows()) jsonGenerator.writeNullField("windows");
                    jsonGenerator.writeEndObject();
                }
            }

            // Append remaining additional field
            for (Map.Entry<String, Object> adE : snapshot.additionalData().entrySet()) {
                jsonGenerator.writeFieldName(adE.getKey());
                jsonGenerator.writeObject(adE.getValue());
            }

            jsonGenerator.writeEndObject();
        }

        /**
         * Writing {@link DependenciesReferenceSet} with given {@link JsonGenerator}.
         *
         * @param drs Applied {@link DependenciesReferenceSet}.
         * @param jg {@link JsonGenerator} which come from
         *           {@link StdSerializer#serialize(Object, JsonGenerator, SerializerProvider)}.
         * @param sdk Version constraint of Dart SDK to determine is
         *            {@link PubspecParsePreference#eligible(PubspecParsePreference, PubSemVerConstraint)}.
         *
         * @throws IOException Encounter problem when writing dependencies to {@link JsonGenerator}.
         */
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

/**
 * Define action with matched {@link DependencyReferenceDictionary}.
 *
 * @param <D> Related {@link DependencyReference} for dictionary.
 *
 * @since 1.0.0
 */
interface DependencyDefinition<D extends DependencyReference> {
    /**
     * Check the structure is matched for given reference type.
     *
     * @param node A node of dependency reference.
     *
     * @return <code>true</code> if is.
     */
    boolean relatedJsonStructure(@Nonnull JsonNode node);

    /**
     * Check is corresponded {@link DependencyReference} for this dictonary.
     *
     * @param ref {@link DependencyReference} for validation.
     *
     * @return <code>true</code> if is.
     */
    boolean isCorrespondedType(@Nonnull DependencyReference ref);

    /**
     * Convert {@link JsonNode} to {@link DependencyReference}.
     *
     * @param name Dependency name.
     * @param node Node of this dependency.
     *
     * @return Object {@link D} which ready to {@link DependenciesReferenceSet#add(DependencyReference)}.
     *
     * @throws Exception When encounter problem during parse.
     */
    @Nonnull
    D jsonToDR(@Nonnull String name, @Nonnull JsonNode node) throws Exception;

    /**
     * Writing {@link DependencyReference} to a file.
     *
     * @param dependencyJsonNode {@link JsonGenerator} that is using to write dependency info.
     * @param ref Dependency reference.
     * @param sdkVC SDK version constraint.
     *
     * @throws IOException Encounter problem when writing.
     */
    void drToJson(
            @Nonnull JsonGenerator dependencyJsonNode,
            @Nonnull DependencyReference ref,
            @Nonnull PubSemVerConstraint sdkVC
    ) throws IOException;
}

/**
 * A dictionary to define the type of {@link DependencyReference}
 *
 * @since 1.0.0
 */
enum DependencyReferenceDictionary {
    /**
     * Refer to {@link HostedReference}.
     */
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
    /**
     * Refer to {@link LocalReference}.
     */
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
    /**
     * Refer to {@link GitReference}.
     */
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
    /**
     * Refer to {@link ThirdPartyHostedReference}.
     */
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
    /**
     * Refer to {@link SDKReference}.
     */
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

    /**
     * An interface that standardize condition and conversion.
     */
    private final DependencyDefinition<? extends DependencyReference> definition;

    /**
     * Assign correspond {@link DependencyReference} type with {@link DependencyDefinition}.
     *
     * @param definition Define condition and conversion.
     */
    DependencyReferenceDictionary(@Nonnull DependencyDefinition<? extends DependencyReference> definition) {
        this.definition = definition;
    }

    /**
     * Convert to {@link DependencyReference} by {@link Map.Entry} which come from {@link JsonNode#fields()}.
     *
     * @param jsonNodeEntry An {@link Map.Entry} from {@link JsonNode#fields()}.
     *
     * @return Corresponded {@link DependencyReference}.
     *
     * @throws Exception When any {@link Exception} thrown during conversion.
     */
    @Nonnull
    public DependencyReference jsonToRef(@Nonnull Map.Entry<String, JsonNode> jsonNodeEntry) throws Exception {
        return definition.jsonToDR(jsonNodeEntry.getKey(), jsonNodeEntry.getValue());
    }

    /**
     * Handle {@link DependencyReference} to write into {@link JsonGenerator}.
     *
     * @param ref {@link DependencyReference}.
     * @param jsonWriter {@link JsonGenerator} which bundled with
     *                   {@link StdSerializer#serialize(Object, JsonGenerator, SerializerProvider)}.
     * @param sdk Dart SDK version constraint.
     *
     * @throws IOException Error encountered when writing to JSON.
     */
    public void refToJson(
            @Nonnull DependencyReference ref,
            @Nonnull JsonGenerator jsonWriter,
            @Nonnull PubSemVerConstraint sdk
    ) throws IOException {
        definition.drToJson(jsonWriter, ref, sdk);
    }

    /**
     * Giving an {@link Object}, and return {@link DependencyReferenceDictionary}'s value depending on what items
     * contain.
     *
     * @param detect An {@link Object} to identify type of {@link DependencyReferenceDictionary}.
     *
     * @return A {@link DependencyReferenceDictionary} that pointing to the eligible value.
     */
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
