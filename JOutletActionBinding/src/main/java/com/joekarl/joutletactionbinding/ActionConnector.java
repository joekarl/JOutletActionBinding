/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.joekarl.joutletactionbinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EventListener;

/**
 *
 * @author karl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.FIELD})
public @interface ActionConnector {
    String action();
    Class<? extends EventListener> actionType();
    String actionListenerMethod();
}
