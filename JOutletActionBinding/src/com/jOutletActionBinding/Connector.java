/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jOutletActionBinding;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author karl
 */
public class Connector {

    private List<Class> visitedClasses = new ArrayList<Class>();

    public void connect(Object outletHolder, Object ui) {
        if (outletHolder != null && ui != null) {
            Field[] outletHolderFields = outletHolder.getClass().getDeclaredFields();
            for (Field outletHolderField : outletHolderFields) {
                if (outletHolderField.isAnnotationPresent(Outlet.class)) {
                    outletHolderField.setAccessible(true);
                    _connectObjectToField(outletHolder, outletHolderField, ui);
                }
            }
            visitedClasses = new ArrayList<Class>();
            _connectActionsToOutletHolder(outletHolder, ui);
        }
    }

    private void _connectObjectToField(Object outletHolder, Field field, Object objectWithOutlets) {
        visitedClasses.add(objectWithOutlets.getClass());
        Field[] objectWithOutletsFields = objectWithOutlets.getClass().getDeclaredFields();
        for (Field objectWithOutletsField : objectWithOutletsFields) {
            objectWithOutletsField.setAccessible(true);
            int modifiers = objectWithOutletsField.getModifiers();
            if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
                continue;
            }
            try {
                List<OutletConnector> connectors = new ArrayList<OutletConnector>();
                OutletConnectors connectorsAnnotation = objectWithOutletsField.getAnnotation(OutletConnectors.class);
                if (connectorsAnnotation != null) {
                    connectors.addAll(Arrays.asList(connectorsAnnotation.value()));
                } else {
                    OutletConnector connector = objectWithOutletsField.getAnnotation(OutletConnector.class);
                    if (connector != null) {
                        connectors.add(connector);
                    }
                }
                if (!connectors.isEmpty()) {
                    for (OutletConnector connector : connectors) {
                        if (connector.outletClass() != null
                                && Arrays.asList(connector.outletClass()).contains(field.getDeclaringClass())
                                && field.getName().equals(connector.fieldName())) {
                            field.set(outletHolder, objectWithOutletsField.get(objectWithOutlets));
                        }
                    }
                } else {
                    if (!visitedClasses.contains(objectWithOutletsField.getType())) {
                        _connectObjectToField(outletHolder, field, objectWithOutletsField.get(objectWithOutlets));
                    }
                }
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Connector.class.getName()).log(Level.SEVERE,
                        "Could not get value of field while binding outlet " + objectWithOutletsField.getName(), ex);
            }
        }
    }

    private void _connectActionsToOutletHolder(Object outletHolder, Object objectWithActions) {
        visitedClasses.add(objectWithActions.getClass());
        if (objectWithActions == null || outletHolder == null) {
            return;
        }
        Field[] objectWithActionsFields = objectWithActions.getClass().getDeclaredFields();
        for (Field objectWithActionsField : objectWithActionsFields) {
            objectWithActionsField.setAccessible(true);
            int modifiers = objectWithActionsField.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            try {
                List<ActionConnector> connectors = new ArrayList<ActionConnector>();
                ActionConnectors connectorsAnnotation = objectWithActionsField.getAnnotation(ActionConnectors.class);
                if (connectorsAnnotation != null) {
                    connectors.addAll(Arrays.asList(connectorsAnnotation.value()));
                } else {
                    ActionConnector connector = objectWithActionsField.getAnnotation(ActionConnector.class);
                    if (connector != null) {
                        connectors.add(connector);
                    }
                }
                if (!connectors.isEmpty()) {
                    _connectActions(connectors, outletHolder, objectWithActionsField.get(objectWithActions));
                } else if (!visitedClasses.contains(objectWithActionsField.get(objectWithActions).getClass())) {
                    _connectActionsToOutletHolder(outletHolder, objectWithActionsField.get(objectWithActions));
                }
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Connector.class.getName()).log(Level.SEVERE,
                        "Could not get value of field while binding actions....", ex);
            }
        }

        List<ActionConnector> connectors = new ArrayList<ActionConnector>();
        ActionConnectors connectorsAnnotation = objectWithActions.getClass().getAnnotation(ActionConnectors.class);
        if (connectorsAnnotation != null) {
            connectors.addAll(Arrays.asList(connectorsAnnotation.value()));
        } else {
            ActionConnector connector = objectWithActions.getClass().getAnnotation(ActionConnector.class);
            if (connector != null) {
                connectors.add(connector);
            }
        }
        if (!connectors.isEmpty()) {
            _connectActions(connectors, outletHolder, objectWithActions);
        }
    }

    private void _connectActions(List<ActionConnector> connectors, Object outletHolder, Object objectWithActions) {
        for (ActionConnector connector : connectors) {
            if (connector != null && Arrays.asList(connector.actionClass()).contains(outletHolder.getClass())) {
                String action = connector.action();
                Class actionType = connector.type();
                String listenerMethod = connector.listenerMethod();
                try {
                    Method actionMethod = null;
                    Method[] methods = outletHolder.getClass().getMethods();
                    for (int i = 0; i < methods.length; ++i) {
                        if (methods[i].getName().equals(action)) {
                            actionMethod = methods[i];
                            break;
                        }
                    }
                    if (actionMethod != null && actionMethod.isAnnotationPresent(Action.class)) {
                        Object genericListener = GenericListener.create(actionType, listenerMethod, outletHolder, action);
                        Method addListenerMethod = objectWithActions.getClass().getMethod("add" + actionType.getSimpleName(), actionType);
                        addListenerMethod.invoke(objectWithActions, actionType.cast(genericListener));
                    }
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(Connector.class.getName()).log(Level.SEVERE,
                            "No method with signature : add" + actionType.getSimpleName(), ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }
}
