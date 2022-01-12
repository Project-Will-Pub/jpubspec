package xyz.rk0cc.willpub.pubspec.parser;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.*;
import xyz.rk0cc.jogu.GitRepositoryURL;
import xyz.rk0cc.jogu.UnknownGitRepositoryURLTypeException;
import xyz.rk0cc.josev.NonStandardSemVerException;
import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubspecConfigurationException;
import xyz.rk0cc.willpub.pubspec.data.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

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
            } catch (
                    IllegalPubspecConfigurationException
                            | NonStandardSemVerException
                            | UnknownGitRepositoryURLTypeException e
            ) {
                throw new IOException("At least one checked exceptions throws when parsing pubspec.yaml", e);
            }
        }

        private static void assignDRFromNode(
                @Nonnull ObjectNode dependenciesNode,
                @Nonnull DependenciesReferenceSet drs
        ) throws IllegalPubPackageNamingException, UnknownGitRepositoryURLTypeException, MalformedURLException {
            final Iterator<Map.Entry<String, JsonNode>> fields = dependenciesNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();

                String depName = entry.getKey();
                JsonNode depRef = entry.getValue();

                switch (DRNodeReference.condition(depRef)) {
                    case HOSTED -> drs.add(
                            new HostedReference(
                                    depName,
                                    PubSemVerConstraint.parse(depRef.isNull() ? null : depRef.textValue())
                            )
                    );
                    case THIRD_PARTY -> {
                        JsonNode hosted = depRef.get("hosted");
                        JsonNode ver = depRef.get("version");

                        if (hosted.isObject())
                            drs.add(new ThirdPartyHostedReference(
                                    depName,
                                    new URL(hosted.get("url").textValue()),
                                    hosted.get("name").textValue(),
                                    PubSemVerConstraint.parse(ver.isMissingNode() ? null : ver.textValue())
                            ));
                        else if (hosted.isTextual())
                            drs.add(new ThirdPartyHostedReference(
                                    depName,
                                    new URL(hosted.textValue()),
                                    PubSemVerConstraint.parse(ver.isMissingNode() ? null : ver.textValue())
                            ));
                    }
                    case GIT -> {
                        JsonNode git = depRef.get("git");

                        if (git.isTextual())
                            drs.add(new GitReference(depName, GitRepositoryURL.parse(git.textValue())));
                        else {
                            JsonNode path = git.get("path"), ref = git.get("ref");

                            drs.add(new GitReference(
                                    depName,
                                    GitRepositoryURL.parse(git.get("url").textValue()),
                                    path.isMissingNode() ? null : path.textValue(),
                                    ref.isMissingNode() ? null : ref.textValue()
                            ));
                        }
                    }
                    case LOCAL -> drs.add(new LocalReference(depName, depRef.get("path").textValue()));
                    case SDK -> {
                        JsonNode ver = depRef.get("version");

                        drs.add(new SDKReference(
                                depName,
                                depRef.get("sdk").textValue(),
                                PubSemVerConstraint.parse(ver.isMissingNode() ? null : ver.textValue())
                        ));
                    }
                }
            }
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

enum DRNodeReference {
    HOSTED(jsonNode -> jsonNode.isNull() || jsonNode.isTextual()),
    THIRD_PARTY(jsonNode -> {
        if (!jsonNode.isObject()) return false;

        JsonNode hosted = jsonNode.get("hosted");

        boolean vh = hosted.isTextual() || (hosted.has("name") || hosted.has("url"));

        JsonNode ver = jsonNode.get("version");

        return vh && (ver.isMissingNode() || ver.isTextual());
    }),
    GIT(jsonNode -> {
        if (!jsonNode.isObject()) return false;

        JsonNode git = jsonNode.get("git");

        if (git.isTextual()) return true;

        JsonNode path = git.get("path"), ref = git.get("ref");

        return git.get("url").isTextual()
                && (path.isMissingNode() || path.isTextual())
                && (ref.isMissingNode() || ref.isTextual());
    }),
    LOCAL(jsonNode -> jsonNode.get("path").isTextual()),
    SDK(jsonNode -> {
        JsonNode ver = jsonNode.get("version");

        return jsonNode.get("sdk").isTextual() && (ver.isMissingNode() || ver.isTextual());
    });

    private final Predicate<JsonNode> cond;

    DRNodeReference(@Nonnull Predicate<JsonNode> cond) {
        this.cond = cond;
    }

    static DRNodeReference condition(JsonNode node) {
        List<DRNodeReference> refs = Arrays.stream(DRNodeReference.values())
                .filter(drnr -> drnr.cond.test(node))
                .toList();

        assert refs.size() == 1;

        return refs.get(0);
    }
}