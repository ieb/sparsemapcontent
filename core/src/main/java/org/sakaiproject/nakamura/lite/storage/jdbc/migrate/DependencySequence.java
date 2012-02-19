package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sakaiproject.nakamura.api.lite.PropertyMigrator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class DependencySequence implements Iterable<PropertyMigrator> {

    private static final Set<PropertyMigrator> EMPTY = ImmutableSet.of();
    private Map<String, Object> runMigrators;
    private Map<String, PropertyMigrator> availableMigrators;
    private Set<PropertyMigrator> toRunSequence;
    private SetView<PropertyMigrator> unresolvedMigrators;

    public DependencySequence(PropertyMigrator[] propertyMigrators, Map<String, Object> runMigratorRecord) {
        toRunSequence = Sets.newLinkedHashSet();
        Set<String> toRunSequenceNames = Sets.newLinkedHashSet();
        availableMigrators = Maps.newLinkedHashMap();
        runMigrators = ImmutableMap.copyOf(runMigratorRecord);
        for (PropertyMigrator pm : propertyMigrators) {
            availableMigrators.put(pm.getName(), pm);
        }
        for (String satisfiedMigrator : runMigrators.keySet()) {
            if (availableMigrators.containsKey(satisfiedMigrator)) {
                if (Boolean.parseBoolean(availableMigrators.get(satisfiedMigrator).getOptions()
                        .get(PropertyMigrator.OPTION_RUNONCE))) {
                    availableMigrators.remove(satisfiedMigrator);
                }
            }
        }
        for (;;) {
            int resolved = 0;
            for (Entry<String, PropertyMigrator> e : availableMigrators.entrySet()) {
                PropertyMigrator pm = e.getValue();
                if (!toRunSequence.contains(pm)) {
                    // migrator has not been run previously and is not in the
                    // list. Check dependencies
                    boolean satisfied = true;
                    for (String dep : pm.getDependencies()) {
                        if ( !availableMigrators.containsKey(dep) ) {
                            if ( !runMigratorRecord.containsKey(dep)) {                        
                                satisfied = false;
                                break;
                            }
                        } else if ( !toRunSequenceNames.contains(dep) ) {
                            satisfied = false;
                            break;                            
                        }
                    }
                    if (satisfied) {
                        toRunSequence.add(pm);
                        toRunSequenceNames.add(pm.getName());
                        resolved++;
                    }
                }
            }
            if (toRunSequence.size() == availableMigrators.size()) {
                // all satisfied
                unresolvedMigrators = null;
                break;
            }
            if (resolved == 0) {
                unresolvedMigrators = Sets.difference(
                        ImmutableSet.copyOf(availableMigrators.values()), toRunSequence);
                break;
            }
        }
    }

    public boolean hasUnresolved() {
        return unresolvedMigrators != null && unresolvedMigrators.size() > 0;
    }

    public Iterator<PropertyMigrator> iterator() {
        if (hasUnresolved()) {
            return EMPTY.iterator();
        }
        return toRunSequence.iterator();
    }

    public Set<PropertyMigrator> getUnresolved() {
        if ( hasUnresolved() ) {
            return ImmutableSet.copyOf(unresolvedMigrators);
        }
        return ImmutableSet.of();
    }

    public Map<String, Object> getAlreadyRun() {
        return runMigrators;
    }
}
