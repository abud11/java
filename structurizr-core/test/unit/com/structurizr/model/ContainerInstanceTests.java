package com.structurizr.model;

import com.structurizr.AbstractWorkspaceTestBase;
import org.junit.Test;

import static org.junit.Assert.*;

public class ContainerInstanceTests extends AbstractWorkspaceTestBase {

    private SoftwareSystem softwareSystem = model.addSoftwareSystem(Location.External, "System", "Description");
    private Container database = softwareSystem.addContainer("Database Schema", "Stores data", "MySQL");
    private DeploymentNode deploymentNode = model.addDeploymentNode("Deployment Node", "Description", "Technology");

    @Test
    public void test_construction() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        assertSame(database, containerInstance.getContainer());
        assertEquals(database.getId(), containerInstance.getContainerId());
        assertEquals(1, containerInstance.getInstanceId());
    }

    @Test
    public void test_getContainerId() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        assertEquals(database.getId(), containerInstance.getContainerId());
        containerInstance.setContainer(null);
        containerInstance.setContainerId("1234");
        assertEquals("1234", containerInstance.getContainerId());
    }

    @Test
    public void test_getName() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        assertNull(containerInstance.getName());

        containerInstance.setName("foo");
        assertNull(containerInstance.getName());
    }

    @Test
    public void test_getCanonicalName() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        assertEquals("/System/Database Schema[1]", containerInstance.getCanonicalName());
    }

    @Test
    public void test_getParent_ReturnsTheParentSoftwareSystem() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        assertEquals(softwareSystem, containerInstance.getParent());
    }

    @Test
    public void test_getRequiredTags() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        assertTrue(containerInstance.getRequiredTags().isEmpty());
    }

    @Test
    public void test_getTags() {
        database.addTags("Database");
        ContainerInstance containerInstance = deploymentNode.add(database);
        containerInstance.addTags("Primary Instance");

        assertEquals("Element,Container,Database,Container Instance,Primary Instance", containerInstance.getTags());
    }

    @Test
    public void test_removeTags_DoesNotRemoveAnyTags() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        assertTrue(containerInstance.getTags().contains(Tags.ELEMENT));
        assertTrue(containerInstance.getTags().contains(Tags.CONTAINER));
        assertTrue(containerInstance.getTags().contains(Tags.CONTAINER_INSTANCE));

        containerInstance.removeTag(Tags.CONTAINER_INSTANCE);
        containerInstance.removeTag(Tags.CONTAINER);
        containerInstance.removeTag(Tags.ELEMENT);

        assertTrue(containerInstance.getTags().contains(Tags.ELEMENT));
        assertTrue(containerInstance.getTags().contains(Tags.CONTAINER));
        assertTrue(containerInstance.getTags().contains(Tags.CONTAINER_INSTANCE));
    }

    @Test
    public void test_uses_ThrowsAnException_WhenADestinationIsNotSpecified() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        try {
            containerInstance.uses(null, "", "");
        } catch (IllegalArgumentException iae) {
            assertEquals("The destination of a relationship must be specified.", iae.getMessage());
        }
    }

    @Test
    public void test_uses_AddsARelationship_WhenADestinationIsSpecified() {
        Container database = softwareSystem.addContainer("Database", "", "");
        ContainerInstance primaryDatabase =  deploymentNode.add(database);
        ContainerInstance secondaryDatabase = deploymentNode.add(database);

        Relationship relationship = primaryDatabase.uses(secondaryDatabase, "Replicates data to", "Some technology");
        assertSame(primaryDatabase, relationship.getSource());
        assertSame(secondaryDatabase, relationship.getDestination());
        assertEquals("Replicates data to", relationship.getDescription());
        assertEquals("Some technology", relationship.getTechnology());
    }

    @Test
    public void test_addHealthCheck() {
        ContainerInstance containerInstance = deploymentNode.add(database);
        assertTrue(containerInstance.getHealthChecks().isEmpty());

        HttpHealthCheck healthCheck = containerInstance.addHealthCheck("Test web application is working", "http://localhost:8080");
        assertEquals("Test web application is working", healthCheck.getName());
        assertEquals("http://localhost:8080", healthCheck.getUrl());
        assertEquals(60, healthCheck.getInterval());
        assertEquals(0, healthCheck.getTimeout());
        assertEquals(1, containerInstance.getHealthChecks().size());
    }

    @Test
    public void test_addHealthCheck_ThrowsAnException_WhenTheNameIsNull() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        try {
            containerInstance.addHealthCheck(null, "http://localhost");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The name must not be null or empty.", iae.getMessage());
        }
    }

    @Test
    public void test_addHealthCheck_ThrowsAnException_WhenTheNameIsEmpty() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        try {
            containerInstance.addHealthCheck(" ", "http://localhost");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The name must not be null or empty.", iae.getMessage());
        }
    }

    @Test
    public void test_addHealthCheck_ThrowsAnException_WhenTheUrlIsNull() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        try {
            containerInstance.addHealthCheck("Name", null);
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The URL must not be null or empty.", iae.getMessage());
        }
    }

    @Test
    public void test_addHealthCheck_ThrowsAnException_WhenTheUrlIsEmpty() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        try {
            containerInstance.addHealthCheck("Name", " ");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The URL must not be null or empty.", iae.getMessage());
        }
    }

    @Test
    public void test_addHealthCheck_ThrowsAnException_WhenTheUrlIsInvalid() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        try {
            containerInstance.addHealthCheck("Name", "localhost");
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("localhost is not a valid URL.", iae.getMessage());
        }
    }

    @Test
    public void test_addHealthCheck_ThrowsAnException_WhenTheIntervalIsLessThanZero() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        try {
            containerInstance.addHealthCheck("Name", "https://localhost", -1, 0);
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The polling interval must be zero or a positive integer.", iae.getMessage());
        }
    }

    @Test
    public void test_addHealthCheck_ThrowsAnException_WhenTheTimeoutIsLessThanZero() {
        ContainerInstance containerInstance = deploymentNode.add(database);

        try {
            containerInstance.addHealthCheck("Name", "https://localhost", 60, -1);
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("The timeout must be zero or a positive integer.", iae.getMessage());
        }
    }

}