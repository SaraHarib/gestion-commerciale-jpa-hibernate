package org.example.entities;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CommandeProduitPKTest {
    @Test
    public void equalsAndHashCodeUseBothIdentifiers() {
        CommandeProduitPK first = new CommandeProduitPK(1L, 2L);
        CommandeProduitPK same = new CommandeProduitPK(1L, 2L);
        CommandeProduitPK third = new CommandeProduitPK(1L, 2L);
        assertEquals(first, first);
        assertEquals(first, same);
        assertEquals(same, first);
        assertEquals(same, third);
        assertEquals(first, third);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, new CommandeProduitPK(2L, 2L));
        assertNotEquals(first, new CommandeProduitPK(1L, 3L));
        assertNotEquals(first, null);
        assertNotEquals(first, "1:2");
        Set<CommandeProduitPK> keys = new HashSet<>();
        keys.add(first);
        assertTrue(keys.contains(same));
    }

    @Test
    public void emptyIdentifierContainersAreNullSafe() {
        CommandeProduitPK first = new CommandeProduitPK();
        CommandeProduitPK second = new CommandeProduitPK();
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        first.setCommandeId(1L);
        assertNotEquals(first, second);
    }
}
