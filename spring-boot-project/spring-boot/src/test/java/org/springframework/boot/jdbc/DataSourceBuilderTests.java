/*
 * Copyright 2012-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.jdbc;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import oracle.jdbc.internal.OpaqueString;
import oracle.jdbc.pool.OracleDataSource;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.apache.commons.dbcp2.BasicDataSource;
import org.h2.Driver;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link DataSourceBuilder}.
 *
 * @author Stephane Nicoll
 * @author Fabio Grassi
 * @author Phillip Webb
 */
class DataSourceBuilderTests {

	private DataSource dataSource;

	@AfterEach
	void shutdownDataSource() throws IOException {
		if (this.dataSource instanceof Closeable) {
			((Closeable) this.dataSource).close();
		}
	}

	@Test
	void buildWhenHikariAvailableReturnsHikariDataSource() {
		this.dataSource = DataSourceBuilder.create().url("jdbc:h2:test").build();
		assertThat(this.dataSource).isInstanceOf(HikariDataSource.class);
		HikariDataSource hikariDataSource = (HikariDataSource) this.dataSource;
		assertThat(hikariDataSource.getJdbcUrl()).isEqualTo("jdbc:h2:test");
	}

	@Test
	void buildWhenHikariNotAvailableReturnsTomcatDataSource() {
		this.dataSource = DataSourceBuilder.create(new HidePackagesClassLoader("com.zaxxer.hikari")).url("jdbc:h2:test")
				.build();
		assertThat(this.dataSource).isInstanceOf(org.apache.tomcat.jdbc.pool.DataSource.class);
	}

	@Test
	void buildWhenHikariAndTomcatNotAvailableReturnsDbcp2DataSource() {
		this.dataSource = DataSourceBuilder
				.create(new HidePackagesClassLoader("com.zaxxer.hikari", "org.apache.tomcat.jdbc.pool"))
				.url("jdbc:h2:test").build();
		assertThat(this.dataSource).isInstanceOf(BasicDataSource.class);
	}

	@Test
	void buildWhenHikariAndTomcatAndDbcpNotAvailableReturnsOracleUcpDataSource() {
		this.dataSource = DataSourceBuilder.create(new HidePackagesClassLoader("com.zaxxer.hikari",
				"org.apache.tomcat.jdbc.pool", "org.apache.commons.dbcp2")).url("jdbc:h2:test").build();
		assertThat(this.dataSource).isInstanceOf(PoolDataSourceImpl.class);
	}

	@Test
	void buildWhenHikariTypeSpecifiedReturnsExpectedDataSource() {
		HikariDataSource hikariDataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
		assertThat(hikariDataSource).isInstanceOf(HikariDataSource.class);
	}

	@Test
	void buildWhenSimpleDriverTypeSpecifiedReturnsExpectedDataSource() {
		this.dataSource = DataSourceBuilder.create().url("jdbc:h2:test").type(SimpleDriverDataSource.class).build();
		assertThat(this.dataSource).isInstanceOf(SimpleDriverDataSource.class);
		SimpleDriverDataSource simpleDriverDataSource = (SimpleDriverDataSource) this.dataSource;
		assertThat(simpleDriverDataSource.getUrl()).isEqualTo("jdbc:h2:test");
		assertThat(simpleDriverDataSource.getDriver()).isInstanceOf(Driver.class);
	}

	@Test
	void buildWhenOracleTypeSpecifiedReturnsExpectedDataSource() throws SQLException {
		this.dataSource = DataSourceBuilder.create().url("jdbc:oracle:thin:@localhost:1521:xe")
				.type(OracleDataSource.class).username("test").build();
		assertThat(this.dataSource).isInstanceOf(OracleDataSource.class);
		OracleDataSource oracleDataSource = (OracleDataSource) this.dataSource;
		assertThat(oracleDataSource.getURL()).isEqualTo("jdbc:oracle:thin:@localhost:1521:xe");
		assertThat(oracleDataSource.getUser()).isEqualTo("test");
	}

	@Test
	void buildWhenOracleUcpTypeSpecifiedReturnsExpectedDataSource() {
		this.dataSource = DataSourceBuilder.create().driverClassName("org.hsqldb.jdbc.JDBCDriver")
				.type(PoolDataSourceImpl.class).username("test").build();
		assertThat(this.dataSource).isInstanceOf(PoolDataSourceImpl.class);
		PoolDataSourceImpl upcDataSource = (PoolDataSourceImpl) this.dataSource;
		assertThat(upcDataSource.getConnectionFactoryClassName()).isEqualTo("org.hsqldb.jdbc.JDBCDriver");
		assertThat(upcDataSource.getUser()).isEqualTo("test");
	}

	@Test
	void buildWhenH2TypeSpecifiedReturnsExpectedDataSource() {
		this.dataSource = DataSourceBuilder.create().url("jdbc:h2:test").type(JdbcDataSource.class).username("test")
				.build();
		assertThat(this.dataSource).isInstanceOf(JdbcDataSource.class);
		JdbcDataSource h2DataSource = (JdbcDataSource) this.dataSource;
		assertThat(h2DataSource.getUser()).isEqualTo("test");
	}

	@Test
	void buildWhenPostgressTypeSpecifiedReturnsExpectedDataSource() {
		this.dataSource = DataSourceBuilder.create().url("jdbc:postgresql://localhost/test")
				.type(PGSimpleDataSource.class).username("test").build();
		assertThat(this.dataSource).isInstanceOf(PGSimpleDataSource.class);
		PGSimpleDataSource pgDataSource = (PGSimpleDataSource) this.dataSource;
		assertThat(pgDataSource.getUser()).isEqualTo("test");
	}

	@Test
	void buildWhenMappedTypeSpecifiedAndNoSuitableMappingThrowsException() {
		assertThatExceptionOfType(UnsupportedDataSourcePropertyException.class).isThrownBy(
				() -> DataSourceBuilder.create().type(OracleDataSource.class).driverClassName("com.example").build());
	}

	@Test
	void buildWhenCustomSubclassTypeSpecifiedReturnsDataSourceWithOnlyBasePropertiesSet() {
		this.dataSource = DataSourceBuilder.create().url("jdbc:h2:test").type(CustomTomcatDataSource.class)
				.username("test").build();
		assertThat(this.dataSource).isInstanceOf(CustomTomcatDataSource.class);
		CustomTomcatDataSource testDataSource = (CustomTomcatDataSource) this.dataSource;
		assertThat(testDataSource.getUrl()).isEqualTo("jdbc:h2:test");
		assertThat(testDataSource.getJdbcUrl()).isNull();
		assertThat(testDataSource.getUsername()).isEqualTo("test");
		assertThat(testDataSource.getUser()).isNull();
		assertThat(testDataSource.getDriverClassName()).isEqualTo(Driver.class.getName());
		assertThat(testDataSource.getDriverClass()).isNull();
	}

	@Test
	void buildWhenCustomTypeSpecifiedReturnsDataSourceWithPropertiesSetViaReflection() {
		this.dataSource = DataSourceBuilder.create().type(CustomDataSource.class).username("test").password("secret")
				.url("jdbc:h2:test").driverClassName("com.example").build();
		assertThat(this.dataSource).isInstanceOf(CustomDataSource.class);
		CustomDataSource testDataSource = (CustomDataSource) this.dataSource;
		assertThat(testDataSource.getUrl()).isEqualTo("jdbc:h2:test");
		assertThat(testDataSource.getUsername()).isEqualTo("test");
		assertThat(testDataSource.getPassword()).isEqualTo("secret");
		assertThat(testDataSource.getDriverClassName()).isEqualTo("com.example");
	}

	@Test
	void buildWhenCustomTypeSpecifiedAndNoSuitableSetterThrowsException() {
		assertThatExceptionOfType(UnsupportedDataSourcePropertyException.class).isThrownBy(() -> DataSourceBuilder
				.create().type(LimitedCustomDataSource.class).driverClassName("com.example").build());
	}

	@Test
	void buildWhenDerivedWithNewUrlReturnsNewDataSource() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setUsername("test");
		dataSource.setPassword("secret");
		dataSource.setJdbcUrl("jdbc:h2:test");
		HikariDataSource built = (HikariDataSource) DataSourceBuilder.derivedFrom(dataSource).url("jdbc:h2:test2")
				.build();
		assertThat(built.getUsername()).isEqualTo("test");
		assertThat(built.getPassword()).isEqualTo("secret");
		assertThat(built.getJdbcUrl()).isEqualTo("jdbc:h2:test2");
	}

	@Test
	void buildWhenDerivedWithNewUsernameAndPasswordReturnsNewDataSource() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setUsername("test");
		dataSource.setPassword("secret");
		dataSource.setJdbcUrl("jdbc:h2:test");
		DataSourceBuilder<?> builder = DataSourceBuilder.derivedFrom(dataSource);
		HikariDataSource built = (HikariDataSource) builder.username("test2").password("secret2").build();
		assertThat(built.getUsername()).isEqualTo("test2");
		assertThat(built.getPassword()).isEqualTo("secret2");
		assertThat(built.getJdbcUrl()).isEqualTo("jdbc:h2:test");
	}

	@Test
	void buildWhenDerivedFromOracleDataSourceWithPasswordNotSetThrowsException() throws Exception {
		oracle.jdbc.datasource.impl.OracleDataSource dataSource = new oracle.jdbc.datasource.impl.OracleDataSource();
		dataSource.setUser("test");
		dataSource.setPassword("secret");
		dataSource.setURL("example.com");
		assertThatExceptionOfType(UnsupportedDataSourcePropertyException.class)
				.isThrownBy(() -> DataSourceBuilder.derivedFrom(dataSource).url("example.org").build());
	}

	@Test
	void buildWhenDerivedFromOracleDataSourceWithPasswordSetReturnsDataSource() throws Exception {
		oracle.jdbc.datasource.impl.OracleDataSource dataSource = new oracle.jdbc.datasource.impl.OracleDataSource();
		dataSource.setUser("test");
		dataSource.setPassword("secret");
		dataSource.setURL("example.com");
		DataSourceBuilder<?> builder = DataSourceBuilder.derivedFrom(dataSource);
		oracle.jdbc.datasource.impl.OracleDataSource built = (oracle.jdbc.datasource.impl.OracleDataSource) builder
				.username("test2").password("secret2").build();
		assertThat(built.getUser()).isEqualTo("test2");
		assertThat(built).extracting("password").extracting((opaque) -> ((OpaqueString) opaque).get())
				.isEqualTo("secret2");
		assertThat(built.getURL()).isEqualTo("example.com");
	}

	@Test
	void buildWhenDerivedFromEmbeddedDatabase() {
		EmbeddedDatabase database = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).build();
		SimpleDriverDataSource built = (SimpleDriverDataSource) DataSourceBuilder.derivedFrom(database).username("test")
				.password("secret").build();
		assertThat(built.getUsername()).isEqualTo("test");
		assertThat(built.getPassword()).isEqualTo("secret");
		assertThat(built.getUrl()).startsWith("jdbc:hsqldb:mem");
	}

	final class HidePackagesClassLoader extends URLClassLoader {

		private final String[] hiddenPackages;

		HidePackagesClassLoader(String... hiddenPackages) {
			super(new URL[0], HidePackagesClassLoader.class.getClassLoader());
			this.hiddenPackages = hiddenPackages;
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			if (Arrays.stream(this.hiddenPackages).anyMatch(name::startsWith)) {
				throw new ClassNotFoundException();
			}
			return super.loadClass(name, resolve);
		}

	}

	static class CustomTomcatDataSource extends org.apache.tomcat.jdbc.pool.DataSource {

		private String jdbcUrl;

		private String user;

		private String driverClass;

		String getJdbcUrl() {
			return this.jdbcUrl;
		}

		void setJdbcUrl(String jdbcUrl) {
			this.jdbcUrl = jdbcUrl;
		}

		String getUser() {
			return this.user;
		}

		void setUser(String user) {
			this.user = user;
		}

		String getDriverClass() {
			return this.driverClass;
		}

		void setDriverClass(String driverClass) {
			this.driverClass = driverClass;
		}

	}

	static class LimitedCustomDataSource extends AbstractDataSource {

		private String username;

		private String password;

		private String url;

		@Override
		public Connection getConnection() throws SQLException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Connection getConnection(String username, String password) throws SQLException {
			throw new UnsupportedOperationException();
		}

		String getUsername() {
			return this.username;
		}

		void setUsername(String username) {
			this.username = username;
		}

		String getPassword() {
			return this.password;
		}

		void setPassword(String password) {
			this.password = password;
		}

		String getUrl() {
			return this.url;
		}

		void setUrl(String url) {
			this.url = url;
		}

	}

	static class CustomDataSource extends LimitedCustomDataSource {

		private String driverClassName;

		String getDriverClassName() {
			return this.driverClassName;
		}

		void setDriverClassName(String driverClassName) {
			this.driverClassName = driverClassName;
		}

	}

}