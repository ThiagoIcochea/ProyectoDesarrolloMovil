/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.utp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.utp.model.Personal;

public interface PersonalRepository extends JpaRepository<Personal, Integer> {
}
