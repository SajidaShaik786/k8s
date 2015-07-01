/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernatePersonDAO;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class PersonDAOTest extends BaseContextSensitiveTest {
	
	private PersonDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		// fetch the dao from the spring application context 
		// this bean name matches the name in /metadata/spring/applicationContext-service.xml 
		dao = (PersonDAO) applicationContext.getBean("personDAO");
	}
	
	/**
	 * @see {@link PersonDAO#getSavedPersonAttributeTypeName(org.openmrs.PersonAttributeType)}
	 */
	@Test
	@Verifies(value = "should get saved personAttributeType name from database", method = "getSavedPersonAttributeTypeName(org.openmrs.PersonAttributeType)")
	public void getSavedPersonAttributeTypeName_shouldGetSavedPersonAttributeTypeNameFromDatabase() throws Exception {
		PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(1);
		
		// save the name from the db for later checks
		String origName = pat.getName();
		String newName = "Race Updated";
		
		assertFalse(newName.equals(origName));
		
		// change the name on the java pojo (NOT in the database)
		pat.setName(newName);
		
		// the value from the database should match the original name from the 
		// pat right after /it/ was fetched from the database
		String nameFromDatabase = dao.getSavedPersonAttributeTypeName(pat);
		assertEquals(origName, nameFromDatabase);
	}
	
	@Test
	@Verifies(value = "should return personName from the DB given valid person name id", method = "getPersonName(Integer)")
	public void getPersonName_shouldGetSavedPersonNameById() throws Exception {
		PersonName personName = dao.getPersonName(2);
		assertEquals(2, (int) personName.getId());
	}
	
	@Test
	@Verifies(value = "should return null from the DB given invalid person name id", method = "getPersonName(Integer)")
	public void getPersonName_shouldNotGetPersonNameGivenInvalidId() throws Exception {
		PersonName personName = dao.getPersonName(-1);
		assertNull(personName);
	}
	
	@Test
	public void savePerson_shouldSavePersonWithBirthDateTime() throws Exception {
		
		executeDataSet("org/openmrs/api/db/include/PersonDAOTest-people.xml");
		
		HibernatePersonDAO hibernatePersonDAO = (HibernatePersonDAO) applicationContext.getBean("personDAO");
		
		Person person = new Person();
		person.setBirthtime(new SimpleDateFormat("HH:mm:ss").parse("15:23:56"));
		person.setBirthdate(new SimpleDateFormat("yyyy-MM-dd").parse("2012-05-29"));
		person.setDead(false);
		person.setVoided(false);
		person.setBirthdateEstimated(false);
		person.setId(345);
		hibernatePersonDAO.savePerson(person);
		
		Person savedPerson = hibernatePersonDAO.getPerson(345);
		Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2012-05-29 15:23:56"), savedPerson
		        .getBirthDateTime());
	}
}
