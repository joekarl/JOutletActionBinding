/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.joekarl.joutletactionbinding;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author karl ctr kirch
 */
public class ViewController {

    private Component view;

    public ViewController() {
    }

    public Component getView(){
        return view;
    }

    public void initWithView(Component view) {
        this.view = view;
        Connector.connect(this, view);
    }

    public void initWithViewClass(Class<? extends Component> view) {
        try {
            this.view = view.newInstance();
            Connector.connect(this, this.view);
        } catch (Exception ex) {
            Logger.getLogger(ViewController.class.getName()).log(Level.SEVERE,
                    "Connection failed from controller " + this.getClass().getSimpleName() + " to view " + view.getSimpleName(),
                    ex);
        }
    }
}
