export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  imageUrl: string;
  categoryId: number; // Relación con la tabla Categories
}

export const _dummy = true; // Agrega esta línea temporalmente