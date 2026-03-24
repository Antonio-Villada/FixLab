/**
 * Utilidades para validación de contraseña en tiempo real:
 * - Requisitos (letra, número, carácter especial, longitud)
 * - Nivel de fortaleza (débil, media, alta)
 */

export interface PasswordRequirements {
  hasLetter: boolean;
  hasNumber: boolean;
  hasSpecial: boolean;
  hasMinLength: boolean;
}

export type PasswordStrength = 'weak' | 'medium' | 'strong';

const MIN_LENGTH = 8;
const STRONG_MIN_LENGTH = 12;

/**
 * Analiza la contraseña y retorna qué requisitos cumple.
 */
export function getPasswordRequirements(password: string): PasswordRequirements {
  if (!password) {
    return { hasLetter: false, hasNumber: false, hasSpecial: false, hasMinLength: false };
  }
  return {
    hasLetter: /[A-Za-z]/.test(password),
    hasNumber: /\d/.test(password),
    hasSpecial: /[^A-Za-z0-9]/.test(password),
    hasMinLength: password.length >= MIN_LENGTH,
  };
}

/**
 * Indica si la contraseña cumple todos los requisitos mínimos.
 */
export function meetsPasswordRequirements(password: string): boolean {
  const req = getPasswordRequirements(password);
  return req.hasLetter && req.hasNumber && req.hasSpecial && req.hasMinLength;
}

/**
 * Calcula la fortaleza de la contraseña.
 * - weak: No cumple requisitos mínimos o es muy predecible
 * - medium: Cumple requisitos (8+ chars, letra, número, especial)
 * - strong: 12+ caracteres con mayúsculas, minúsculas, números y especiales
 */
export function getPasswordStrength(password: string): PasswordStrength {
  if (!password || password.length < MIN_LENGTH) return 'weak';

  const hasLetter = /[A-Za-z]/.test(password);
  const hasNumber = /\d/.test(password);
  const hasSpecial = /[^A-Za-z0-9]/.test(password);
  const hasUpper = /[A-Z]/.test(password);
  const hasLower = /[a-z]/.test(password);

  if (!hasLetter || !hasNumber || !hasSpecial) return 'weak';

  // Cumple mínimo
  if (password.length >= STRONG_MIN_LENGTH && hasUpper && hasLower && hasNumber && hasSpecial) {
    return 'strong';
  }

  // Cumple requisitos básicos
  return 'medium';
}

/**
 * Etiqueta en español para la fortaleza.
 */
export function getStrengthLabel(strength: PasswordStrength): string {
  return strength === 'weak' ? 'Débil' : strength === 'medium' ? 'Media' : 'Alta';
}
