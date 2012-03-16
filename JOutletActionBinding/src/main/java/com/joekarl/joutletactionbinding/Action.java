/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.joekarl.joutletactionbinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author karl ctr kirch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {

}
