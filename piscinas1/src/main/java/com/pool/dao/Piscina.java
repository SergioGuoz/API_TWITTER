/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pool.dao;

import com.pool.modelo.Pool;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sergio
 */
public class Piscina {
 
    public static List<Pool> getPools(){
        List<Pool> lista_state= new ArrayList();
        Pool p= new Pool(0,0);
        Pool po= new Pool(1,0);
        
        lista_state.add(p);
        lista_state.add(po);
        return lista_state;
    }
}
