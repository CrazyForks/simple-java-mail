package org.simplejavamail.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simplejavamail.api.email.ContentTransferEncoding;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.config.ConfigLoader.Property;
import testutil.ConfigLoaderTestHelper;

import java.io.ByteArrayInputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.simplejavamail.api.email.ContentTransferEncoding.BINARY;
import static org.simplejavamail.api.mailer.config.TransportStrategy.SMTPS;
import static org.simplejavamail.config.ConfigLoader.Property.CUSTOM_SSLFACTORY_CLASS;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_BCC_ADDRESS;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_BCC_NAME;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_CC_ADDRESS;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_CC_NAME;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_CONTENT_TRANSFER_ENCODING;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_FROM_ADDRESS;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_FROM_NAME;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_REPLYTO_ADDRESS;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_REPLYTO_NAME;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_SUBJECT;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_TO_ADDRESS;
import static org.simplejavamail.config.ConfigLoader.Property.DEFAULT_TO_NAME;
import static org.simplejavamail.config.ConfigLoader.Property.DISABLE_ALL_CLIENTVALIDATION;
import static org.simplejavamail.config.ConfigLoader.Property.EMBEDDEDIMAGES_DYNAMICRESOLUTION_BASE_CLASSPATH;
import static org.simplejavamail.config.ConfigLoader.Property.EMBEDDEDIMAGES_DYNAMICRESOLUTION_BASE_DIR;
import static org.simplejavamail.config.ConfigLoader.Property.EMBEDDEDIMAGES_DYNAMICRESOLUTION_BASE_URL;
import static org.simplejavamail.config.ConfigLoader.Property.EXTRA_PROPERTIES;
import static org.simplejavamail.config.ConfigLoader.Property.JAVAXMAIL_DEBUG;
import static org.simplejavamail.config.ConfigLoader.Property.PROXY_HOST;
import static org.simplejavamail.config.ConfigLoader.Property.PROXY_PASSWORD;
import static org.simplejavamail.config.ConfigLoader.Property.PROXY_PORT;
import static org.simplejavamail.config.ConfigLoader.Property.PROXY_SOCKS5BRIDGE_PORT;
import static org.simplejavamail.config.ConfigLoader.Property.PROXY_USERNAME;
import static org.simplejavamail.config.ConfigLoader.Property.SMIME_ENCRYPTION_CERTIFICATE;
import static org.simplejavamail.config.ConfigLoader.Property.SMIME_SIGNING_KEYSTORE;
import static org.simplejavamail.config.ConfigLoader.Property.SMIME_SIGNING_KEYSTORE_PASSWORD;
import static org.simplejavamail.config.ConfigLoader.Property.SMIME_SIGNING_KEY_ALIAS;
import static org.simplejavamail.config.ConfigLoader.Property.SMIME_SIGNING_KEY_PASSWORD;
import static org.simplejavamail.config.ConfigLoader.Property.SMTP_HOST;
import static org.simplejavamail.config.ConfigLoader.Property.SMTP_PASSWORD;
import static org.simplejavamail.config.ConfigLoader.Property.SMTP_PORT;
import static org.simplejavamail.config.ConfigLoader.Property.SMTP_USERNAME;
import static org.simplejavamail.config.ConfigLoader.Property.TRANSPORT_STRATEGY;

public class ConfigLoaderTest {

	@BeforeEach
	public void restoreOriginalStaticProperties() {
		ConfigLoader.loadProperties("simplejavamail.properties", false);
		System.getProperties().clear();
	}

	@Test
	public void valueOrProperty()
			throws Exception {
		Map<Property, Object> properties = new HashMap<>();
		properties.put(TRANSPORT_STRATEGY, "preconfiguredValue");
		ConfigLoaderTestHelper.setResolvedProperties(properties);

		assertThat(ConfigLoader.valueOrProperty("value", TRANSPORT_STRATEGY)).isEqualTo("value");
		assertThat(ConfigLoader.valueOrProperty((String) null, TRANSPORT_STRATEGY)).isEqualTo("preconfiguredValue");
		assertThat(ConfigLoader.valueOrProperty((String) null, SMTP_HOST)).isNull();
	}

	@Test
	public void valueOrPropertyDefaultValue()
			throws Exception {
		Map<Property, Object> properties = new HashMap<>();
		properties.put(TRANSPORT_STRATEGY, "preconfiguredValue");
		ConfigLoaderTestHelper.setResolvedProperties(properties);

		assertThat(ConfigLoader.valueOrPropertyAsString("value", TRANSPORT_STRATEGY, "backup")).isEqualTo("value");
		assertThat(ConfigLoader.valueOrPropertyAsString(null, TRANSPORT_STRATEGY, "backup")).isEqualTo("preconfiguredValue");
		assertThat(ConfigLoader.valueOrPropertyAsString(null, SMTP_HOST, "backup")).isEqualTo("backup");
		assertThat(ConfigLoader.valueOrPropertyAsString(null, SMTP_HOST, null)).isNull();
	}

	@Test
	public void valueOrPropertyEmptyDefaultValue()
			throws Exception {
		Map<Property, Object> properties = new HashMap<>();
		properties.put(TRANSPORT_STRATEGY, "default");
		ConfigLoaderTestHelper.setResolvedProperties(properties);

		assertThat(ConfigLoader.valueOrPropertyAsString("value", TRANSPORT_STRATEGY, "backup")).isEqualTo("value");
		assertThat(ConfigLoader.valueOrPropertyAsString(null, TRANSPORT_STRATEGY, "backup")).isEqualTo("default");
		assertThat(ConfigLoader.valueOrPropertyAsString("", TRANSPORT_STRATEGY, "backup")).isEqualTo("default");
		assertThat(ConfigLoader.valueOrPropertyAsString(null, TRANSPORT_STRATEGY, null)).isEqualTo("default");
	}

	@Test
	public void valueOrPropertyDefaultValueEmptyDefault()
			throws Exception {
		Map<Property, Object> properties = new HashMap<>();
		properties.put(TRANSPORT_STRATEGY, "");
		ConfigLoaderTestHelper.setResolvedProperties(properties);

		assertThat(ConfigLoader.valueOrPropertyAsString("value", TRANSPORT_STRATEGY, "backup")).isEqualTo("value");
		assertThat(ConfigLoader.valueOrPropertyAsString(null, TRANSPORT_STRATEGY, "backup")).isEqualTo("backup");
		assertThat(ConfigLoader.valueOrPropertyAsString("", TRANSPORT_STRATEGY, "backup")).isEqualTo("backup");
		assertThat(ConfigLoader.valueOrPropertyAsString(null, TRANSPORT_STRATEGY, null)).isNull();
	}

	@Test
	public void overridefromSystemVariables() {
		assertThat(ConfigLoader.valueOrPropertyAsString("value", PROXY_USERNAME, "backup")).isEqualTo("value");
		assertThat(ConfigLoader.valueOrPropertyAsString(null, PROXY_USERNAME, "backup")).isEqualTo("username proxy"); // from config file
		// cannot be tested:
		//		System.getenv().put("simplejavamail.proxy.username", "override1");
		//		restoreOriginalStaticProperties();
		//		assertThat(ConfigLoader.valueOrProperty(null, PROXY_USERNAME, "backup")).isEqualTo("override1");
		System.out.println("simplejavamail.proxy.username" + ": " + System.getProperty("simplejavamail.proxy.username"));
		System.setProperty("simplejavamail.proxy.username", "override2");
		System.out.println("simplejavamail.proxy.username" + ": " + System.getProperty("simplejavamail.proxy.username"));
		restoreOriginalStaticProperties();
		assertThat(ConfigLoader.valueOrPropertyAsString(null, PROXY_USERNAME, "backup")).isEqualTo("override2");
	}

	@Test
	public void hasProperty()
			throws Exception {
		Map<Property, Object> properties = new HashMap<>();
		properties.put(TRANSPORT_STRATEGY, "preconfiguredValue1");
		properties.put(DEFAULT_FROM_ADDRESS, "preconfiguredValue2");
		properties.put(DEFAULT_BCC_NAME, null);
		ConfigLoaderTestHelper.setResolvedProperties(properties);

		assertThat(ConfigLoader.hasProperty(TRANSPORT_STRATEGY)).isTrue();
		assertThat(ConfigLoader.hasProperty(DEFAULT_FROM_ADDRESS)).isTrue();
		assertThat(ConfigLoader.hasProperty(DEFAULT_BCC_NAME)).isFalse();
		assertThat(ConfigLoader.hasProperty(PROXY_HOST)).isFalse();
	}

	@Test
	public void getProperty()
			throws Exception {
		Map<Property, Object> properties = new HashMap<>();
		properties.put(TRANSPORT_STRATEGY, "preconfiguredValue1");
		properties.put(DEFAULT_FROM_ADDRESS, "preconfiguredValue2");
		properties.put(DEFAULT_BCC_NAME, null);
		ConfigLoaderTestHelper.setResolvedProperties(properties);

		assertThat(ConfigLoader.<String>getProperty(TRANSPORT_STRATEGY)).isEqualTo("preconfiguredValue1");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_FROM_ADDRESS)).isEqualTo("preconfiguredValue2");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_BCC_NAME)).isNull();
		assertThat(ConfigLoader.<String>getProperty(PROXY_HOST)).isNull();
	}

	@Test
	public void parsePropertyValue() {
		assertThat(ConfigLoader.parsePropertyValue("simple string value")).isEqualTo("simple string value");
		assertThat(ConfigLoader.parsePropertyValue("12345")).isEqualTo(12345);
		assertThat(ConfigLoader.parsePropertyValue("123d45")).isEqualTo("123d45");
		assertThat(ConfigLoader.parsePropertyValue("0")).isEqualTo(false);
		assertThat(ConfigLoader.parsePropertyValue("false")).isEqualTo(false);
		assertThat(ConfigLoader.parsePropertyValue("no")).isEqualTo(false);
		assertThat(ConfigLoader.parsePropertyValue("1")).isEqualTo(true);
		assertThat(ConfigLoader.parsePropertyValue("true")).isEqualTo(true);
		assertThat(ConfigLoader.parsePropertyValue("yes")).isEqualTo(true);
		assertThat(ConfigLoader.parsePropertyValue("yesno")).isEqualTo("yesno");
		assertThat(ConfigLoader.parsePropertyValue("SMTP")).isEqualTo(TransportStrategy.SMTP);
		assertThat(ConfigLoader.parsePropertyValue("SMTP_TLS")).isEqualTo(TransportStrategy.SMTP_TLS);
	}

	@Test
	public void loadPropertiesFromFileClassPath() {
		ConfigLoader.loadProperties("simplejavamail.properties", false);
		assertThat(ConfigLoader.<Boolean>getProperty(JAVAXMAIL_DEBUG)).isEqualTo(true);
		assertThat(ConfigLoader.<TransportStrategy>getProperty(TRANSPORT_STRATEGY)).isSameAs(SMTPS);

		assertThat(ConfigLoader.<String>getProperty(SMTP_HOST)).isEqualTo("smtp.default.com");
		assertThat(ConfigLoader.<Integer>getProperty(SMTP_PORT)).isEqualTo(25);
		assertThat(ConfigLoader.<String>getProperty(SMTP_USERNAME)).isEqualTo("username");
		assertThat(ConfigLoader.<String>getProperty(SMTP_PASSWORD)).isEqualTo("password");

		assertThat(ConfigLoader.<String>getProperty(PROXY_HOST)).isEqualTo("proxy.default.com");
		assertThat(ConfigLoader.<Integer>getProperty(PROXY_PORT)).isEqualTo(1080);
		assertThat(ConfigLoader.<String>getProperty(PROXY_USERNAME)).isEqualTo("username proxy");
		assertThat(ConfigLoader.<String>getProperty(PROXY_PASSWORD)).isEqualTo("password proxy");
		assertThat(ConfigLoader.<Boolean>getProperty(DISABLE_ALL_CLIENTVALIDATION)).isFalse();
		assertThat(ConfigLoader.<Integer>getProperty(PROXY_SOCKS5BRIDGE_PORT)).isEqualTo(1081);

		assertThat(ConfigLoader.<String>getProperty(DEFAULT_FROM_NAME)).isEqualTo("From Default");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_FROM_ADDRESS)).isEqualTo("from@default.com");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_REPLYTO_NAME)).isEqualTo("Reply-To Default");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_REPLYTO_ADDRESS)).isEqualTo("reply-to@default.com");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_TO_NAME)).isEqualTo("To Default");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_TO_ADDRESS)).isEqualTo("to@default.com");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_CC_NAME)).isEqualTo("CC Default");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_CC_ADDRESS)).isEqualTo("cc@default.com");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_BCC_NAME)).isEqualTo("BCC Default");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_BCC_ADDRESS)).isEqualTo("bcc@default.com");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_SUBJECT)).isEqualTo("Default Subject");
		assertThat(ConfigLoader.<ContentTransferEncoding>getProperty(DEFAULT_CONTENT_TRANSFER_ENCODING)).isSameAs(BINARY);

		assertThat(ConfigLoader.<String>getProperty(SMIME_SIGNING_KEYSTORE)).isEqualTo("src/test/resources/pkcs12/smime_keystore.pkcs12");
		assertThat(ConfigLoader.<String>getProperty(SMIME_SIGNING_KEYSTORE_PASSWORD)).isEqualTo("letmein");
		assertThat(ConfigLoader.<String>getProperty(SMIME_SIGNING_KEY_ALIAS)).isEqualTo("smime_test_user_alias_rsa");
		assertThat(ConfigLoader.<String>getProperty(SMIME_SIGNING_KEY_PASSWORD)).isEqualTo("letmein");
		assertThat(ConfigLoader.<String>getProperty(SMIME_ENCRYPTION_CERTIFICATE)).isEqualTo("src/test/resources/pkcs12/smime_test_user.pem.standard.crt");

		assertThat(ConfigLoader.<String>getProperty(EMBEDDEDIMAGES_DYNAMICRESOLUTION_BASE_DIR)).isEqualTo("");
		assertThat(ConfigLoader.<String>getProperty(EMBEDDEDIMAGES_DYNAMICRESOLUTION_BASE_URL)).isEqualTo("");
		assertThat(ConfigLoader.<String>getProperty(EMBEDDEDIMAGES_DYNAMICRESOLUTION_BASE_CLASSPATH)).isEqualTo("");
	}

	@Test
	public void loadPropertiesAddingMode() {
		String s1 = "simplejavamail.javaxmail.debug=true\n"
				+ "simplejavamail.transportstrategy=SMTPS";
		String s2 = "simplejavamail.defaults.to.name=To Default\n"
				+ "simplejavamail.defaults.to.address=to@default.com";

		ConfigLoader.loadProperties(new ByteArrayInputStream(s1.getBytes()), false);
		ConfigLoader.loadProperties(new ByteArrayInputStream(s2.getBytes()), true);

		// some checks from the config file
		assertThat(ConfigLoader.<Boolean>getProperty(JAVAXMAIL_DEBUG)).isEqualTo(true);
		assertThat(ConfigLoader.<TransportStrategy>getProperty(TRANSPORT_STRATEGY)).isEqualTo(TransportStrategy.SMTPS);
		// now check if the extra properties were added
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_TO_NAME)).isEqualTo("To Default");
		assertThat(ConfigLoader.<String>getProperty(DEFAULT_TO_ADDRESS)).isEqualTo("to@default.com");
	}

	@Test
	public void loadPropertiesFromInputStream() {
		String s = "simplejavamail.javaxmail.debug=true\n"
				+ "simplejavamail.transportstrategy=SMTPS\n"
				+ "simplejavamail.smtp.host=smtp.default.com\n"
				+ "simplejavamail.smtp.port=25\n"
				+ "simplejavamail.smtp.username=username\n"
				+ "simplejavamail.smtp.password=password\n"
				+ "simplejavamail.custom.sslfactory.class=teh_class\n";

		ConfigLoader.loadProperties(new ByteArrayInputStream(s.getBytes()), false);
		assertThat(ConfigLoader.<Boolean>getProperty(JAVAXMAIL_DEBUG)).isEqualTo(true);
		assertThat(ConfigLoader.<TransportStrategy>getProperty(TRANSPORT_STRATEGY)).isSameAs(SMTPS);
		assertThat(ConfigLoader.<String>getProperty(SMTP_HOST)).isEqualTo("smtp.default.com");
		assertThat(ConfigLoader.<Integer>getProperty(SMTP_PORT)).isEqualTo(25);
		assertThat(ConfigLoader.<String>getProperty(SMTP_USERNAME)).isEqualTo("username");
		assertThat(ConfigLoader.<String>getProperty(SMTP_PASSWORD)).isEqualTo("password");
		assertThat(ConfigLoader.<String>getProperty(CUSTOM_SSLFACTORY_CLASS)).isEqualTo("teh_class");
	}

	@Test
	public void loadPropertiesFromProperties() {
		Properties source = new Properties();
		source.put("simplejavamail.javaxmail.debug", "true");
		source.put("simplejavamail.transportstrategy", "SMTPS");
		source.put("simplejavamail.smtp.host", "smtp.default.com");
		source.put("simplejavamail.smtp.port", "25");
		source.put("simplejavamail.smtp.username", "username");
		source.put("simplejavamail.smtp.password", "password");
		source.put("simplejavamail.custom.sslfactory.class", "teh_class");
		source.put("simplejavamail.extraproperties.a", "A");
		source.put("simplejavamail.extraproperties.b", "B");

		ConfigLoader.loadProperties(source, false);
		assertThat(ConfigLoader.<Boolean>getProperty(JAVAXMAIL_DEBUG)).isEqualTo(true);
		assertThat(ConfigLoader.<TransportStrategy>getProperty(TRANSPORT_STRATEGY)).isSameAs(SMTPS);
		assertThat(ConfigLoader.<String>getProperty(SMTP_HOST)).isEqualTo("smtp.default.com");
		assertThat(ConfigLoader.<Integer>getProperty(SMTP_PORT)).isEqualTo(25);
		assertThat(ConfigLoader.<String>getProperty(SMTP_USERNAME)).isEqualTo("username");
		assertThat(ConfigLoader.<String>getProperty(SMTP_PASSWORD)).isEqualTo("password");
		assertThat(ConfigLoader.<String>getProperty(CUSTOM_SSLFACTORY_CLASS)).isEqualTo("teh_class");
		assertThat(ConfigLoader.<Map<String, String>>getProperty(EXTRA_PROPERTIES))
				.containsExactly(new SimpleEntry<>("a", "A"), new SimpleEntry<>("b", "B"));
	}

	@Test
	public void loadPropertiesFromObjectProperties() {
		Properties source = new Properties();
		source.put("simplejavamail.javaxmail.debug", true);
		source.put("simplejavamail.transportstrategy", TransportStrategy.SMTPS);
		source.put("simplejavamail.smtp.host", "smtp.default.com");
		source.put("simplejavamail.smtp.port", 25);
		source.put("simplejavamail.smtp.username", "username");
		source.put("simplejavamail.smtp.password", "password");
		source.put("simplejavamail.custom.sslfactory.class", "teh_class");

		ConfigLoader.loadProperties(source, false);
		assertThat(ConfigLoader.<Boolean>getProperty(JAVAXMAIL_DEBUG)).isEqualTo(true);
		assertThat(ConfigLoader.<TransportStrategy>getProperty(TRANSPORT_STRATEGY)).isSameAs(SMTPS);
		assertThat(ConfigLoader.<String>getProperty(SMTP_HOST)).isEqualTo("smtp.default.com");
		assertThat(ConfigLoader.<Integer>getProperty(SMTP_PORT)).isEqualTo(25);
		assertThat(ConfigLoader.<String>getProperty(SMTP_USERNAME)).isEqualTo("username");
		assertThat(ConfigLoader.<String>getProperty(SMTP_PASSWORD)).isEqualTo("password");
		assertThat(ConfigLoader.<String>getProperty(CUSTOM_SSLFACTORY_CLASS)).isEqualTo("teh_class");
	}

	@Test
	public void loadPropertiesFileNotAvailable() {
		ConfigLoader.loadProperties("non-existent.properties", false);
		// success: no error occurred while config file was missing
	}

	@Test
	public void loadPropertiesFileMalformed() {
		assertThatThrownBy(() -> ConfigLoader.loadProperties("malformed.properties", false))
				.describedAs("error: malformed properties file should cause an illegal argument exception")
				.isInstanceOf(IllegalArgumentException.class);
	}
}