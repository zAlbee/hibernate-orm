/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.propertyref.partial;

import org.hibernate.Hibernate;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.NotImplementedYet;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.Test;

import static org.hibernate.cfg.AvailableSettings.DEFAULT_CACHE_CONCURRENCY_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Gavin King
 */
@ServiceRegistry( settings = @Setting( name = DEFAULT_CACHE_CONCURRENCY_STRATEGY, value = "nonstrict-read-write" ) )
@DomainModel( xmlMappings = "org/hibernate/orm/test/propertyref/partial/Mapping.hbm.xml" )
@SessionFactory
@NotImplementedYet( strict = false )
public class PartialComponentPropertyRefTest {

// need to simply comment this out as the failure here occurs while building the SF which is the fixture
//	@Test
	public void testComponentPropertyRef(SessionFactoryScope scope) {
		scope.inTransaction( (s) -> {
			Person p = new Person();
			p.setIdentity( new Identity() );
			Account a = new Account();
			a.setNumber("123-12345-1236");
			a.setOwner(p);
			p.getIdentity().setName("Gavin");
			p.getIdentity().setSsn("123-12-1234");
			s.persist(p);
			s.persist(a);
		} );

		scope.inTransaction( (s) -> {
			Account a = s.createQuery("from Account a left join fetch a.owner", Account.class ).uniqueResult();
			assertTrue( Hibernate.isInitialized( a.getOwner() ) );
			assertNotNull( a.getOwner() );
			assertEquals( "Gavin", a.getOwner().getIdentity().getName() );
		} );

		scope.inTransaction( (s) -> {
			Account a = s.get(Account.class, "123-12345-1236");
			assertFalse( Hibernate.isInitialized( a.getOwner() ) );
			assertNotNull( a.getOwner() );
			assertEquals( "Gavin", a.getOwner().getIdentity().getName() );
			assertTrue( Hibernate.isInitialized( a.getOwner() ) );
		} );

		scope.getSessionFactory().getCache().evictEntityData( Account.class );
		scope.getSessionFactory().getCache().evictEntityData( Person.class );

		Account a = scope.fromTransaction( (s) -> {
			Account acct = s.get(Account.class, "123-12345-1236");
			assertTrue( Hibernate.isInitialized( acct.getOwner() ) );
			assertNotNull( acct.getOwner() );
			assertEquals( "Gavin", acct.getOwner().getIdentity().getName() );
			assertTrue( Hibernate.isInitialized( acct.getOwner() ) );

			return acct;
		} );

		scope.inTransaction( (s) -> {
			s.remove( a );
			s.remove( a.getOwner() );
		} );
	}
}

