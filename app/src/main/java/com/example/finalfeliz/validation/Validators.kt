package com.example.finalfeliz.validation

// Nombre real: solo letras (incluye acentos) y espacios, mínimo 2 caracteres
private val NAME_REGEX = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]{2,}$")

// Email con @ y un punto DESPUÉS del @ (dominio válido básico)
private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

// Password: mínimo 8, al menos 1 mayúscula, 1 minúscula, 1 dígito y 1 caracter especial
private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\W_]).{8,}$")

fun isValidName(name: String) = NAME_REGEX.matches(name.trim())
fun isValidEmail(email: String) = EMAIL_REGEX.matches(email.trim())
fun isStrongPassword(pass: String) = PASSWORD_REGEX.matches(pass)
