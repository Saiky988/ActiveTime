package com.saiky.activetime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TimeControllerTest {

    private ConfigManager configManager;
    private MockTickAdapter tickAdapter;
    private Logger logger;
    private List<String> broadcastedMessages;
    private TimeController controller;

    private static class MockTickAdapter implements TickAdapter {
        private boolean frozen = false;
        private int setFrozenCalls = 0;

        @Override
        public boolean isFrozen() {
            return frozen;
        }

        @Override
        public void setFrozen(boolean frozen) {
            this.frozen = frozen;
            this.setFrozenCalls++;
        }
    }

    @BeforeEach
    void setUp() {
        configManager = mock(ConfigManager.class);
        when(configManager.isEnabled()).thenReturn(true);
        when(configManager.isFreezeWhenEmpty()).thenReturn(true);
        when(configManager.isMessagesEnabled()).thenReturn(true);
        when(configManager.getMessagePrefix()).thenReturn("[ActiveTime] ");
        when(configManager.getFreezeMessage()).thenReturn("Frozen");
        when(configManager.getUnfreezeMessage()).thenReturn("Resumed");

        tickAdapter = new MockTickAdapter();
        logger = Logger.getLogger("ActiveTimeTest");
        broadcastedMessages = new ArrayList<>();

        controller = new TimeController(configManager, tickAdapter, logger, broadcastedMessages::add);
    }

    @Test
    @DisplayName("Empty server on startup should freeze simulation")
    void testStartupEmptyServerFreezes() {
        controller.reconcile(0);

        assertTrue(controller.isFrozen());
        assertEquals(SimulationState.FROZEN, controller.getState());
        assertTrue(controller.isFrozenByActiveTime());
        assertEquals(1, tickAdapter.setFrozenCalls);
        assertEquals(1, broadcastedMessages.size());
        assertEquals("[ActiveTime] Frozen", broadcastedMessages.get(0));
    }

    @Test
    @DisplayName("Player joining frozen server should unfreeze simulation")
    void testPlayerJoinsUnfreezesServer() {
        // Initially empty and frozen
        controller.reconcile(0);
        assertTrue(controller.isFrozen());

        // Player joins
        controller.reconcile(1);

        assertFalse(controller.isFrozen());
        assertEquals(SimulationState.RUNNING, controller.getState());
        assertFalse(controller.isFrozenByActiveTime());
        assertEquals(2, tickAdapter.setFrozenCalls); // 1 freeze + 1 unfreeze
        assertEquals(2, broadcastedMessages.size());
        assertEquals("[ActiveTime] Resumed", broadcastedMessages.get(1));
    }

    @Test
    @DisplayName("Multiple players leaving should not freeze until last player quits")
    void testMultiplePlayersLeaving() {
        // 2 players online
        controller.reconcile(2);
        assertFalse(controller.isFrozen());
        assertEquals(0, tickAdapter.setFrozenCalls);

        // 1 player leaves, 1 remains
        controller.reconcile(1);
        assertFalse(controller.isFrozen());
        assertEquals(0, tickAdapter.setFrozenCalls);

        // Last player leaves -> 0 players remain
        controller.reconcile(0);
        assertTrue(controller.isFrozen());
        assertEquals(1, tickAdapter.setFrozenCalls);
    }

    @Test
    @DisplayName("Redundant reconciliation events should not spam freeze/unfreeze calls")
    void testRedundantReconciliationDoesNotSpam() {
        // Freeze once
        controller.reconcile(0);
        assertEquals(1, tickAdapter.setFrozenCalls);

        // Another empty check should be a no-op
        controller.reconcile(0);
        assertEquals(1, tickAdapter.setFrozenCalls);

        // Unfreeze once
        controller.reconcile(1);
        assertEquals(2, tickAdapter.setFrozenCalls);

        // Another player joins (1 -> 2)
        controller.reconcile(2);
        assertEquals(2, tickAdapter.setFrozenCalls);
    }

    @Test
    @DisplayName("Disabling freeze.when-empty should prevent automatic freeze")
    void testFreezeWhenEmptyDisabled() {
        when(configManager.isFreezeWhenEmpty()).thenReturn(false);

        controller.reconcile(0);

        assertFalse(controller.isFrozen());
        assertEquals(0, tickAdapter.setFrozenCalls);
        assertEquals(0, broadcastedMessages.size());
    }

    @Test
    @DisplayName("Disabling master plugin setting should ignore events")
    void testMasterDisabledIgnoresEvents() {
        when(configManager.isEnabled()).thenReturn(false);

        controller.reconcile(0);

        assertFalse(controller.isFrozen());
        assertEquals(0, tickAdapter.setFrozenCalls);
    }

    @Test
    @DisplayName("Manual freeze and unfreeze transitions")
    void testManualFreezeAndUnfreeze() {
        assertTrue(controller.freeze(true));
        assertTrue(controller.isFrozen());
        assertFalse(controller.isFrozenByActiveTime()); // Was manual, not auto

        // Redundant freeze
        assertFalse(controller.freeze(true));

        // Manual unfreeze
        assertTrue(controller.unfreeze(true));
        assertFalse(controller.isFrozen());

        // Redundant unfreeze
        assertFalse(controller.unfreeze(true));
    }

    @Test
    @DisplayName("Shutdown safety should unfreeze server if frozen by ActiveTime")
    void testShutdownSafety() {
        // Auto-frozen by empty server
        controller.reconcile(0);
        assertTrue(controller.isFrozen());
        assertTrue(controller.isFrozenByActiveTime());

        // Shutdown triggers safety restore
        controller.onShutdown();

        assertFalse(controller.isFrozen());
        assertEquals(SimulationState.RUNNING, controller.getState());
        assertFalse(controller.isFrozenByActiveTime());
    }
}

