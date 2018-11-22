package com.quorum.tessera.config.util;

import com.quorum.tessera.io.IOCallback;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.jaxb.MarshallerBuilder;
import com.quorum.tessera.config.util.jaxb.UnmarshallerBuilder;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

public final class JaxbUtil {

    public static final Class[] JAXB_CLASSES = new Class[]{
        Config.class,
        KeyDataConfig.class,
        PrivateKeyData.class,
        ArgonOptions.class,
        JdbcConfig.class,
        KeyData.class,
        Peer.class,
        PrivateKeyType.class,
        ServerConfig.class,
        DeprecatedServerConfig.class,
        ServerSocket.class,
        InetServerSocket.class,
        UnixServerSocket.class,
        SslAuthenticationMode.class,
        SslConfig.class,
        SslTrustMode.class
    };

    private JaxbUtil() {
    }

    public static <T> T unmarshal(InputStream inputStream, Class<T> type) {

        try {
            return UnmarshallerBuilder.create()
                    .build().unmarshal(new StreamSource(inputStream), type).getValue();
        } catch (JAXBException ex) {
            throw new ConfigException(ex);
        }
    }

    public static void marshal(Object object, OutputStream outputStream) {
        try {
            MarshallerBuilder.create().build()
                    .marshal(object, outputStream);
        } catch (Throwable ex) {
            Optional<ConstraintViolationException> validationException = unwrapConstraintViolationException(ex);
            if (validationException.isPresent()) {
                throw validationException.get();
            }
            throw new ConfigException(ex);
        }

    }

    public static void marshalWithNoValidation(Object object, OutputStream outputStream) {
        try {
            MarshallerBuilder.create()
                    .withoutBeanValidation()
                    .build()
                    .marshal(object, outputStream);

        } catch (JAXBException ex) {
            throw new ConfigException(ex);
        }
    }

    public static String marshalToString(Object object) {
        return IOCallback.execute(() -> {
            try (OutputStream out = new ByteArrayOutputStream()) {
                marshal(object, out);
                return out.toString();
            }
        });
    }

    public static String marshalToStringNoValidation(Object object) {
        return IOCallback.execute(() -> {
            try (OutputStream out = new ByteArrayOutputStream()) {
                marshalWithNoValidation(object, out);
                return out.toString();
            }
        });
    }

    protected static Optional<ConstraintViolationException> unwrapConstraintViolationException(Throwable ex) {
        return Optional.of(ex)
                .map(Throwable::getCause)
                .filter(Objects::nonNull)
                .filter(ConstraintViolationException.class::isInstance)
                .map(ConstraintViolationException.class::cast);
    }

    public static void marshalMasked(Config object, OutputStream outputStream) {

        XmlProcessingCallback.execute(() -> {

            Marshaller marshaller = MarshallerBuilder.create()
                    .withXmlMediaType()
                    .withoutBeanValidation()
                    .build();

            String xmlData;
            try (StringWriter writer = new StringWriter()) {
                marshaller.marshal(object, writer);
                xmlData = writer.toString();
            }

            StreamSource xmlSource = new StreamSource(new StringReader(xmlData));
            try (StringWriter writer = new StringWriter()) {
                StreamResult xmlResult = new StreamResult(writer);
                createMaskingXslTransformer().transform(xmlSource, xmlResult);
                writer.flush();

                Unmarshaller unmarshaller = UnmarshallerBuilder.create()
                        .withXmlMediaType()
                        .withoutBeanValidation()
                        .build();

                Config masked = (Config) unmarshaller.unmarshal(new StringReader(writer.toString()));

                marshalWithNoValidation(masked, outputStream);
                return null;
            }

        });

    }

    private static Transformer createMaskingXslTransformer() {
        return XmlProcessingCallback.execute(() -> {
            try (InputStream inputStream = JaxbUtil.class.getResourceAsStream("/xsl/mask-config.xsl")) {
                return TransformerFactory.newInstance().newTransformer(new StreamSource(inputStream));
            }
        });

    }

}
