import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs'; // 'of' crea un Observable con datos estáticos
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  
  // Esta es la lista de "mentira" (Mock) que usaremos por ahora
  private mockProducts: Product[] = [
    {
      id: 1,
      name: 'Laptop HP Pavilion',
      description: 'Procesador Intel i7, 16GB RAM, 512GB SSD.',
      price: 2500000,
      stock: 12,
      imageUrl: 'images/Laptop-acer.jpg', // Se busca en: public/images/laptop.jpg
      categoryId: 1
    },
    {
      id: 2,
      name: 'Mouse Gamer RGB',
      description: 'Sensor óptico de alta precisión, 6 botones programables.',
      price: 2100000,
      stock: 50,
      imageUrl: 'images/Laptop-hp.jpg', // Se busca en: public/images/mouse.jpg
      categoryId: 2
    },
    {
      id: 3,
      name: 'Monitor 27" 4K',
      description: 'Panel IPS, 144Hz, compatible con HDR10.',
      price: 3200000,
      stock: 8,
      imageUrl: 'images/Laptop-aple.jpg', // Se busca en: public/images/monitor.jpg
      categoryId: 1
    },
    {
      id: 4,
      name: 'Monitor 27" 4K',
      description: 'Panel IPS, 144Hz, compatible con HDR10.',
      price: 1900000,
      stock: 8,
      imageUrl: 'images/Laptop-Nitro.jpg', // Se busca en: public/images/monitor.jpg
      categoryId: 1
    },
    {
      id: 5,
      name: 'Maus',
      description: 'Sensor óptico de alta precisión, 6 botones programables.',
      price: 100000,
      stock: 8,
      imageUrl: 'images/maus.jpg', // Se busca en: public/images/monitor.jpg
      categoryId: 3
    },
     {
      id: 6,
      name: 'Router WiFi 6',
      description: 'Rendimiento ultra rápido, cobertura ampliada, ideal para hogares inteligentes.',
      price: 320000,
      stock: 8,
      imageUrl: 'images/router.jpg', // Se busca en: public/images/router.jpg
      categoryId: 4
    }
  ];

  constructor() { }

  // Simulamos la petición al servidor devolviendo el array estático
  getProducts(): Observable<Product[]> {
    return of(this.mockProducts);
  }
}