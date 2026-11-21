import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, catchError, tap } from 'rxjs';
import { Product, Offer, OfferEvent, ResolvedCart, Order, CartLine } from './models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/v1';

  // State signals
  readonly cart = signal<ResolvedCart>({
    buyerId: 1,
    lines: [],
    total: '0.000'
  });

  readonly activeOffers = signal<Offer[]>([]);
  readonly pendingVendorOffers = signal<Offer[]>([]);

  readonly orders = signal<Order[]>([
    {
      id: 1042,
      buyerId: 1,
      totalAmount: '143.500',
      codAmount: '143.500',
      status: 'SHIPPED',
      shippingAddress: 'شارع الحبيب بورقيبة، تونس العاصمة',
      phone: '+216 98 123 456',
      createdAt: new Date(Date.now() - 36 * 3600 * 1000).toISOString(),
      subOrders: [
        {
          id: 201,
          orderId: 1042,
          vendorId: 1,
          subTotal: '125.000',
          commissionAmount: '12.500',
          status: 'SHIPPED',
          courierId: 'RAPID_POST_TN',
          trackingNo: 'RP-TN-782914',
          createdAt: new Date(Date.now() - 36 * 3600 * 1000).toISOString()
        },
        {
          id: 202,
          orderId: 1042,
          vendorId: 2,
          subTotal: '18.500',
          commissionAmount: '1.850',
          status: 'CONFIRMED',
          courierId: 'ARAMEX_TN',
          trackingNo: 'ARX-TN-992301',
          createdAt: new Date(Date.now() - 36 * 3600 * 1000).toISOString()
        }
      ]
    }
  ]);

  // Seeded Tunisian products fallback data with 4:5 real photography assets
  readonly initialProducts: Product[] = [
    {
      productId: 1,
      slug: 'sfax-extra-virgin-olive-oil-5l',
      vendorId: 1,
      name: 'زيت زيتون بكر ممتاز صفاقس 5 لتر',
      description: 'زيت زيتون طبيعي بكر ممتاز معصور على البارد من حقول صفاقس الغناء، ذو نكهة أصيلة وحموضة منخفضة جداً.',
      locale: 'ar',
      skuId: 1,
      skuCode: 'SKU-ZIT-SFAX-5L',
      price: '125.000',
      stockOnHand: 50,
      availableStock: 50,
      imageUrl: 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=800&auto=format&fit=crop&q=80',
      governorate: 'صفاقس',
      region: 'Sfax',
      acceptsOffers: true
    },
    {
      productId: 2,
      slug: 'harissa-arbi-nabeul-1kg',
      vendorId: 1,
      name: 'هريسة عربي أصيلة نابل 1 كغ',
      description: 'هريسة دياري محضرة بالطرق التقليدية بفلفل الوطن القبلي المجفف شمسياً مع الثوم والكروية والكزبرة وزيت الزيتون.',
      locale: 'ar',
      skuId: 2,
      skuCode: 'SKU-HRS-NAB-1KG',
      price: '18.500',
      stockOnHand: 100,
      availableStock: 98,
      imageUrl: 'https://images.unsplash.com/photo-1596797038530-2c107229654b?w=800&auto=format&fit=crop&q=80',
      governorate: 'نابل',
      region: 'Nabeul',
      acceptsOffers: true
    },
    {
      productId: 3,
      slug: 'tozeur-deglet-nour-dates-2kg',
      vendorId: 1,
      name: 'دقلة النور توزر فرز أول 2 كغ',
      description: 'تمر دقلة النور الذهبي الفاخر من واحات الجريد بتوزر، قوام طري وشفاف وعسل طبيعي لا يضاهى.',
      locale: 'ar',
      skuId: 3,
      skuCode: 'SKU-DGL-TOZ-2KG',
      price: '28.000',
      stockOnHand: 40,
      availableStock: 40,
      imageUrl: 'https://images.unsplash.com/photo-1546548970-71785318a17b?w=800&auto=format&fit=crop&q=80',
      governorate: 'توزر',
      region: 'Tozeur',
      acceptsOffers: true
    },
    {
      productId: 4,
      slug: 'djerba-guellala-tagine-artisanat',
      vendorId: 2,
      name: 'طاجين فخار قلالة جربة أصيل',
      description: 'فخار يدوي أصيل مصنوع من طين جزيرة جربة في قرية قلالة التراثية، مخصص للطهي الصحي البطيء.',
      locale: 'ar',
      skuId: 4,
      skuCode: 'SKU-POT-JER-001',
      price: '45.000',
      stockOnHand: 15,
      availableStock: 15,
      imageUrl: 'https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=800&auto=format&fit=crop&q=80',
      governorate: 'مدنين',
      region: 'Djerba',
      acceptsOffers: true
    }
  ];

  // 1. Search products
  getProductBySlug(slug: string): Observable<Product | undefined> {
    return this.http.get<Product>(`${this.baseUrl}/products/${slug}`)
      .pipe(
        catchError(() => {
          const found = this.initialProducts.find(p => p.slug === slug);
          return of(found);
        })
      );
  }

  getOrders(): Observable<Order[]> {
    return of(this.orders());
  }

  searchProducts(query: string = '', lang: string = 'ar'): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/products?q=${encodeURIComponent(query)}&lang=${lang}`)
      .pipe(
        catchError(() => {
          // If offline/backend not connected yet, use rich client-side search with normalizer
          if (!query.trim()) return of(this.initialProducts);
          const q = query.toLowerCase();
          const filtered = this.initialProducts.filter(p => 
            p.name.toLowerCase().includes(q) || 
            p.description.toLowerCase().includes(q) ||
            p.region?.toLowerCase().includes(q) ||
            (q.includes('zit') && p.slug.includes('olive')) ||
            (q.includes('7rissa') && p.slug.includes('harissa')) ||
            (q.includes('degla') && p.slug.includes('dates'))
          );
          return of(filtered.length ? filtered : this.initialProducts);
        })
      );
  }

  // 2. Submit Haggling Offer
  submitOffer(skuId: number, qty: number, amountDinar: string): Observable<Offer> {
    const body = { skuId, qty, amountDinar };
    return this.http.post<Offer>(`${this.baseUrl}/offers`, body)
      .pipe(
        catchError(() => {
          // Local fallback simulation with exact auto-rules
          const sku = this.initialProducts.find(p => p.skuId === skuId);
          const listTotal = sku ? parseFloat(sku.price) * qty : 50;
          const proposed = parseFloat(amountDinar);
          const pct = (proposed / listTotal) * 100;

          let status: Offer['status'] = 'PENDING';
          let lockExpiresAt: string | undefined = undefined;

          if (pct >= 95) {
            status = 'ACCEPTED';
            const d = new Date();
            d.setMinutes(d.getMinutes() + 30);
            lockExpiresAt = d.toISOString();
          } else if (pct < 70) {
            status = 'REJECTED';
          }

          const mockOffer: Offer = {
            id: Date.now(),
            skuId,
            buyerId: 1,
            vendorId: sku?.vendorId || 1,
            qty,
            listPrice: listTotal.toFixed(3),
            currentAmount: proposed.toFixed(3),
            status,
            round: 1,
            expiresAt: new Date(Date.now() + 24 * 3600 * 1000).toISOString(),
            lockExpiresAt,
            version: 0,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          };

          this.activeOffers.update(list => [mockOffer, ...list]);
          if (mockOffer.status === 'PENDING') {
            this.pendingVendorOffers.update(list => [mockOffer, ...list]);
          }

          return of(mockOffer);
        }),
        tap(offer => {
          this.activeOffers.update(list => {
            const exists = list.some(o => o.id === offer.id);
            return exists ? list.map(o => o.id === offer.id ? offer : o) : [offer, ...list];
          });
        })
      );
  }

  // 3. Vendor Accept / Counter / Reject
  vendorAccept(offerId: number): Observable<Offer> {
    return this.http.post<Offer>(`${this.baseUrl}/offers/${offerId}/accept`, {})
      .pipe(
        catchError(() => {
          const d = new Date();
          d.setMinutes(d.getMinutes() + 30);
          this.activeOffers.update(list => list.map(o => o.id === offerId ? {
            ...o,
            status: 'ACCEPTED',
            lockExpiresAt: d.toISOString()
          } : o));
          const updated = this.activeOffers().find(o => o.id === offerId)!;
          return of(updated);
        })
      );
  }

  vendorReject(offerId: number): Observable<Offer> {
    return this.http.post<Offer>(`${this.baseUrl}/offers/${offerId}/reject`, {})
      .pipe(
        catchError(() => {
          this.activeOffers.update(list => list.map(o => o.id === offerId ? { ...o, status: 'REJECTED' } : o));
          return of(this.activeOffers().find(o => o.id === offerId)!);
        })
      );
  }

  vendorCounter(offerId: number, counterDinar: string): Observable<Offer> {
    return this.http.post<Offer>(`${this.baseUrl}/offers/${offerId}/counter`, { amountDinar: counterDinar })
      .pipe(
        catchError(() => {
          this.activeOffers.update(list => list.map(o => o.id === offerId ? {
            ...o,
            status: 'COUNTERED',
            currentAmount: counterDinar,
            round: o.round + 1
          } : o));
          return of(this.activeOffers().find(o => o.id === offerId)!);
        })
      );
  }

  // 4. Cart management
  addToCart(skuId: number, qty: number, offerId?: number): Observable<any> {
    const body = { skuId, qty, offerId };
    return this.http.post(`${this.baseUrl}/cart/items`, body)
      .pipe(
        catchError(() => {
          const product = this.initialProducts.find(p => p.skuId === skuId)!;
          const offer = offerId ? this.activeOffers().find(o => o.id === offerId) : undefined;
          
          const unitPrice = offer ? (parseFloat(offer.currentAmount) / offer.qty).toFixed(3) : product.price;
          const lineTotal = (parseFloat(unitPrice) * qty).toFixed(3);

          const newLine: CartLine = {
            cartItemId: Date.now(),
            skuId,
            skuCode: product.skuCode,
            vendorId: product.vendorId,
            qty,
            offerId,
            isOfferLocked: !!offer,
            unitPrice,
            lineTotal,
            productName: product.name
          };

          this.cart.update(c => {
            const lines = [...c.lines.filter(l => l.skuId !== skuId), newLine];
            const total = lines.reduce((acc, l) => acc + parseFloat(l.lineTotal), 0).toFixed(3);
            return { ...c, lines, total };
          });

          return of(newLine);
        })
      );
  }

  getCart(): Observable<ResolvedCart> {
    return this.http.get<ResolvedCart>(`${this.baseUrl}/cart`)
      .pipe(
        catchError(() => of(this.cart())),
        tap(c => this.cart.set(c))
      );
  }

  clearCart(): void {
    this.cart.set({ buyerId: 1, lines: [], total: '0.000' });
  }

  // 5. Checkout Saga
  checkout(cmd: any, idempotencyKey?: string): Observable<any> {
    const headers = idempotencyKey ? new HttpHeaders({ 'Idempotency-Key': idempotencyKey }) : undefined;
    return this.http.post(`${this.baseUrl}/checkout`, cmd, { headers })
      .pipe(
        catchError(() => {
          const mockOrder: Order = {
            id: Math.floor(1000 + Math.random() * 9000),
            buyerId: 1,
            totalAmount: this.cart().total,
            codAmount: cmd.paymentGateway === 'COD' ? this.cart().total : '0.000',
            status: 'PLACED',
            shippingAddress: cmd.shippingAddress,
            phone: cmd.phone,
            createdAt: new Date().toISOString(),
            subOrders: [
              {
                id: Math.floor(100 + Math.random() * 900),
                orderId: 1001,
                vendorId: 1,
                subTotal: this.cart().total,
                commissionAmount: (parseFloat(this.cart().total) * 0.1).toFixed(3),
                status: 'PLACED',
                courierId: 'RAPID_POST_TN',
                trackingNo: 'RP-TN-' + Math.floor(100000 + Math.random() * 900000),
                createdAt: new Date().toISOString()
              }
            ]
          };
          this.orders.update(list => [mockOrder, ...list]);
          this.clearCart();
          return of({
            orderId: mockOrder.id,
            status: 'PLACED',
            totalAmount: mockOrder.totalAmount,
            paymentGateway: cmd.paymentGateway,
            paymentRef: 'COD-' + mockOrder.id
          });
        }),
        tap(() => this.clearCart())
      );
  }
}
