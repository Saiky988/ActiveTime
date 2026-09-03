package com.saiky.activetime.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class ActiveTimeControllerTest {

    private ActiveTimeConfig config;
    private MockTickAdapter tickAdapter;
    private Logger logger;
    private List<String> messages;
    private ActiveTimeController controller;

    private static class MockTickAdapter implements TickManagerAdapter {
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
        config = new ActiveTimeConfig(true, true, true, "[ActiveTime] ", "Frozen", "Resumed");
        tickAdapter = new MockTickAdapter();
        logger = Logger.getLogger("ActiveTimeTest");
        messages = new ArrayList<>();
        controller = new ActiveTimeController(config, tickAdapter, logger, messages::add);
    }

    @Test
    @DisplayName("Empty server on startup should freeze simulation")
    void testStartupEmptyServerFreezes() {
        controller.reconcile(0);

        assertTrue(controller.isFrozen());
        assertEquals(ActiveTimeState.FROZEN, controller.getState());
        assertTrue(controller.isFrozenByActiveTime());
        assertEquals(1, tickAdapter.setFrozenCalls);
        assertEquals(1, messages.size());
        assertEquals("[ActiveTime] Frozen", messages.get(0));
    }

    @Test
    @DisplayName("Player joining frozen server should unfreeze simulation")
    void testPlayerJoinsUnfreezesServer() {
        controller.reconcile(0);
        assertTrue(controller.isFrozen());

        controller.reconcile(1);

        assertFalse(controller.isFrozen());
        assertEquals(ActiveTimeState.RUNNING, controller.getState());
        assertFalse(controller.isFrozenByActiveTime());
        assertEquals(2, tickAdapter.setFrozenCalls);
        assertEquals(2, messages.size());
        assertEquals("[ActiveTime] Resumed", messages.get(1));
    }

    @Test
    @DisplayName("Multiple players leaving should not freeze until last player quits")
    void testMultiplePlayersLeaving() {
        controller.reconcile(2);
        assertFalse(controller.isFrozen());
        assertEquals(0, tickAdapter.setFrozenCalls);

        controller.reconcile(1);
        assertFalse(controller.isFrozen());
        assertEquals(0, tickAdapter.setFrozenCalls);

        controller.reconcile(0);
        assertTrue(controller.isFrozen());
        assertEquals(1, tickAdapter.setFrozenCalls);
    }

    @Test
    @DisplayName("Redundant reconciliation events should not spam freeze/unfreeze calls")
    void testRedundantReconciliationDoesNotSpam() {
        controller.reconcile(0);
        assertEquals(1, tickAdapter.setFrozenCalls);

        controller.reconcile(0);
        assertEquals(1, tickAdapter.setFrozenCalls);

        controller.reconcile(1);
        assertEquals(2, tickAdapter.setFrozenCalls);

        controller.reconcile(2);
        assertEquals(2, tickAdapter.setFrozenCalls);
    }

    @Test
    @DisplayName("Disabling freeze.when-empty should prevent automatic freeze")
    void testFreezeWhenEmptyDisabled() {
        config.setFreezeWhenEmpty(false);

        controller.reconcile(0);

        assertFalse(controller.isFrozen());
        assertEquals(0, tickAdapter.setFrozenCalls);
    }

    @Test
    @DisplayName("Disabling master plugin setting should ignore events")
    void testMasterDisabledIgnoresEvents() {
        config.setEnabled(false);

        controller.reconcile(0);

        assertFalse(controller.isFrozen());
        assertEquals(0, tickAdapter.setFrozenCalls);
    }

    @Test
    @DisplayName("Manual freeze and unfreeze transitions")
    void testManualFreezeAndUnfreeze() {
        assertTrue(controller.freeze(true));
        assertTrue(controller.isFrozen());
        assertFalse(controller.isFrozenByActiveTime());

        assertFalse(controller.freeze(true));

        assertTrue(controller.unfreeze(true));
        assertFalse(controller.isFrozen());

        assertFalse(controller.unfreeze(true));
    }

    @Test
    @DisplayName("Shutdown safety should unfreeze server if frozen by ActiveTime")
    void testShutdownSafety() {
        controller.reconcile(0);
        assertTrue(controller.isFrozen());

        controller.onShutdown();

        assertFalse(controller.isFrozen());
        assertEquals(ActiveTimeState.RUNNING, controller.getState());
        assertFalse(controller.isFrozenByActiveTime());
    }
}
