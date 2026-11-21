import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/catalog/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'search',
    loadComponent: () => import('./features/catalog/search.component').then(m => m.SearchComponent)
  },
  {
    path: 'product/:slug',
    loadComponent: () => import('./features/catalog/product-detail.component').then(m => m.ProductDetailComponent)
  },
  {
    path: 'cart',
    loadComponent: () => import('./features/cart/cart.component').then(m => m.CartComponent)
  },
  {
    path: 'checkout',
    loadComponent: () => import('./features/checkout/checkout.component').then(m => m.CheckoutComponent)
  },
  {
    path: 'orders',
    loadComponent: () => import('./features/orders/orders.component').then(m => m.OrdersComponent)
  },
  {
    path: 'vendor',
    loadComponent: () => import('./features/vendor/vendor-portal.component').then(m => m.VendorPortalComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
