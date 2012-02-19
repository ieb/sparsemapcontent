package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.PropertyMigrator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class DependencySequenceTest {

    private static final Map<String, Object> EMPTY_MAP = ImmutableMap.of();
    private static final String[] EMPTY_STRING = new String[0];
    private static final Map<String, String> EMPTY_STRING_MAP = ImmutableMap.of();

    @Test
    public void testEmptySequence() {
        PropertyMigrator[] propertyMigrators = new PropertyMigrator[0];
        DependencySequence dependencySequence = new DependencySequence(propertyMigrators, EMPTY_MAP);
        Assert.assertFalse(dependencySequence.hasUnresolved());
        Assert.assertEquals(0, dependencySequence.getUnresolved().size());
        Assert.assertEquals(0, getSize(dependencySequence));
        Assert.assertEquals(0, getSize(dependencySequence.getAlreadyRun()));
    }

    @Test
    public void testResolvableSequence() {
        PropertyMigrator[] propertyMigrators = new PropertyMigrator[] {
                new TPropertyMigrator("test1", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test2", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test3", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test4", EMPTY_STRING, EMPTY_STRING_MAP) };
        DependencySequence dependencySequence = new DependencySequence(propertyMigrators, EMPTY_MAP);
        Assert.assertFalse(dependencySequence.hasUnresolved());
        Assert.assertEquals(0, dependencySequence.getUnresolved().size());
        Assert.assertArrayEquals(propertyMigrators, getArray(dependencySequence));
        Assert.assertEquals(0, getSize(dependencySequence.getAlreadyRun()));
    }

    @Test
    public void testUnResolvableSequence() {
        PropertyMigrator[] propertyMigrators = new PropertyMigrator[] {
                new TPropertyMigrator("test1", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test2", new String[] { "test22" }, EMPTY_STRING_MAP),
                new TPropertyMigrator("test3", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test4", EMPTY_STRING, EMPTY_STRING_MAP) };
        DependencySequence dependencySequence = new DependencySequence(propertyMigrators, EMPTY_MAP);
        Assert.assertTrue(dependencySequence.hasUnresolved());
        Assert.assertArrayEquals(new PropertyMigrator[] { propertyMigrators[1] },
                getArray(dependencySequence.getUnresolved()));
        Assert.assertArrayEquals(new PropertyMigrator[0], getArray(dependencySequence));
        Assert.assertEquals(0, getSize(dependencySequence.getAlreadyRun()));
    }

    @Test
    public void testResolvableAlreadyRunSequence() {
        PropertyMigrator[] propertyMigrators = new PropertyMigrator[] {
                new TPropertyMigrator("test1", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test2", new String[] { "test22" }, EMPTY_STRING_MAP),
                new TPropertyMigrator("test3", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test4", EMPTY_STRING, EMPTY_STRING_MAP) };
        DependencySequence dependencySequence = new DependencySequence(propertyMigrators,
                ImmutableMap.of("test22", (Object) "122312312;0"));
        Assert.assertFalse(dependencySequence.hasUnresolved());
        Assert.assertArrayEquals(new PropertyMigrator[0],
                getArray(dependencySequence.getUnresolved()));
        Assert.assertArrayEquals(propertyMigrators, getArray(dependencySequence));
        Assert.assertArrayEquals(new String[] { "test22" }, getArray(dependencySequence
                .getAlreadyRun().keySet()));
    }

    @Test
    public void testResolvableAlreadyRunUnspecifiedSequence() {
        PropertyMigrator[] propertyMigrators = new PropertyMigrator[] {
                new TPropertyMigrator("test1", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test2", new String[] { "test22" }, EMPTY_STRING_MAP),
                new TPropertyMigrator("test3", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test4", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test22", EMPTY_STRING, EMPTY_STRING_MAP) };
        DependencySequence dependencySequence = new DependencySequence(propertyMigrators,
                ImmutableMap.of("test22", (Object) "122312312;0"));
        Assert.assertArrayEquals(new String[] { "test22" }, getArray(dependencySequence
                .getAlreadyRun().keySet()));
        Assert.assertArrayEquals(new PropertyMigrator[0],
                getArray(dependencySequence.getUnresolved()));
        Assert.assertArrayEquals(new PropertyMigrator[] {
                propertyMigrators[0], propertyMigrators[2], propertyMigrators[3], propertyMigrators[4], propertyMigrators[1]
        }, getArray(dependencySequence));
    }

    @Test
    public void testResolvableAlreadyRunOnceSequence() {
        PropertyMigrator[] propertyMigrators = new PropertyMigrator[] {
                new TPropertyMigrator("test1", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test2", new String[] { "test22" }, EMPTY_STRING_MAP),
                new TPropertyMigrator("test3", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test4", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test22", EMPTY_STRING, ImmutableMap.of(
                        PropertyMigrator.OPTION_RUNONCE, "false")) };
        DependencySequence dependencySequence = new DependencySequence(propertyMigrators,
                ImmutableMap.of("test22", (Object) "122312312;0"));
        Assert.assertFalse(dependencySequence.hasUnresolved());
        Assert.assertArrayEquals(new PropertyMigrator[0],
                getArray(dependencySequence.getUnresolved()));
        Assert.assertArrayEquals(new PropertyMigrator[] {
                propertyMigrators[0], propertyMigrators[2], propertyMigrators[3], propertyMigrators[4], propertyMigrators[1]
        }, getArray(dependencySequence));
        Assert.assertArrayEquals(new String[] { "test22" }, getArray(dependencySequence
                .getAlreadyRun().keySet()));
    }

    @Test
    public void testResolvableAlreadyRunOnlyOnceSequence() {
        PropertyMigrator[] propertyMigrators = new PropertyMigrator[] {
                new TPropertyMigrator("test1", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test2", new String[] { "test22" }, EMPTY_STRING_MAP),
                new TPropertyMigrator("test3", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test4", EMPTY_STRING, EMPTY_STRING_MAP),
                new TPropertyMigrator("test22", EMPTY_STRING, ImmutableMap.of(
                        PropertyMigrator.OPTION_RUNONCE, "true")) };
        DependencySequence dependencySequence = new DependencySequence(propertyMigrators,
                ImmutableMap.of("test22", (Object) "122312312;0"));
        Assert.assertFalse(dependencySequence.hasUnresolved());
        Assert.assertArrayEquals(new PropertyMigrator[0],
                getArray(dependencySequence.getUnresolved()));
        Assert.assertArrayEquals((new PropertyMigrator[] { propertyMigrators[0],
                propertyMigrators[1], propertyMigrators[2], propertyMigrators[3] }),
                getArray(dependencySequence));
        Assert.assertArrayEquals(new String[] { "test22" }, getArray(dependencySequence
                .getAlreadyRun().keySet()));
    }

    @SuppressWarnings("unchecked")
    private <T> T[] getArray(Iterable<T> iterator) {
        List<T> l = Lists.newArrayList();
        for (T t : iterator) {
            l.add(t);
        }
        return (T[]) l.toArray();
    }

    private <T> int getSize(Iterable<T> iterable) {
        int i = 0;
        for (@SuppressWarnings("unused") T t : iterable) {
            i++;
        }
        return i;
    }

    private int getSize(Map<String, Object> alreadyRun) {
        return alreadyRun.size();
    }

}
