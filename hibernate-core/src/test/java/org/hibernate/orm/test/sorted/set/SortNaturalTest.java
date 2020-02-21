package org.hibernate.orm.test.sorted.set;

import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.SortNatural;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings( "unused" )
public class SortNaturalTest extends BaseNonConfigCoreFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Owner.class, Cat.class };
	}

	@Test
	@TestForIssue( jiraKey = "HHH-8827" )
	public void testSortNatural() {
		Session s = openSession();
		s.beginTransaction();

		Owner owner = new Owner();
		Cat cat1 = new Cat();
		Cat cat2 = new Cat();
		cat1.owner = owner;
		cat1.name = "B";
		cat2.owner = owner;
		cat2.name = "A";
		owner.cats.add( cat1 );
		owner.cats.add( cat2 );
		s.persist( owner );

		s.getTransaction().commit();
		s.clear();

		s.beginTransaction();

		owner = s.get( Owner.class, owner.id );
		assertNotNull( owner.cats );
		assertEquals( 2, owner.cats.size() );
		assertEquals( "A", owner.cats.first().name );
		assertEquals( "B", owner.cats.last().name );

		s.getTransaction().commit();
		s.close();
	}

	@Entity(name = "Owner")
	@Table(name = "Owner")
	private static class Owner {

		@Id
		@GeneratedValue
		private long id;

		@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
		@SortNatural
		private SortedSet<Cat> cats = new TreeSet<>();
	}

	@Entity(name = "Cat")
	@Table(name = "Cat")
	private static class Cat implements Comparable<Cat> {

		@Id
		@GeneratedValue
		private long id;

		@ManyToOne
		private Owner owner;

		private String name;

		@Override
		public int compareTo(Cat other) {
			return this.name.compareTo( other.name );
		}
	}
}
