export interface Product {
  productId: number;
  slug: string;
  vendorId: number;
  name: string;
  description: string;
  locale: string;
  skuId: number;
  skuCode: string;
  price: string; // e.g. "12.500"
  stockOnHand: number;
  availableStock: number;
  matchScore?: number;
  imageUrl?: string;
  governorate?: string;
  region?: string;
  acceptsOffers?: boolean;
}

export interface Offer {
  id: number;
  skuId: number;
  buyerId: number;
  vendorId: number;
  qty: number;
  listPrice: string;
  currentAmount: string;
  status: 'PENDING' | 'COUNTERED' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED' | 'WITHDRAWN' | 'CONVERTED';
  round: number;
  expiresAt: string;
  lockExpiresAt?: string;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface OfferEvent {
  id: number;
  offerId: number;
  actor: 'BUYER' | 'VENDOR' | 'SYSTEM';
  type: 'SUBMITTED' | 'COUNTERED' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED' | 'WITHDRAWN';
  amount: string;
  createdAt: string;
}

export interface CartLine {
  cartItemId: number;
  skuId: number;
  skuCode: string;
  vendorId: number;
  qty: number;
  offerId?: number;
  isOfferLocked: boolean;
  unitPrice: string;
  lineTotal: string;
  productName?: string;
}

export interface ResolvedCart {
  buyerId: number;
  lines: CartLine[];
  total: string;
}

export interface VendorSubOrder {
  id: number;
  orderId: number;
  vendorId: number;
  subTotal: string;
  commissionAmount: string;
  status: 'PLACED' | 'CONFIRMED' | 'VENDOR_ACCEPTED' | 'SHIPPED' | 'DELIVERED' | 'SETTLED' | 'CANCELLED';
  courierId?: string;
  trackingNo?: string;
  createdAt: string;
}

export interface Order {
  id: number;
  buyerId: number;
  totalAmount: string;
  codAmount: string;
  status: string;
  shippingAddress: string;
  phone: string;
  governorateId?: number;
  idempotencyKey?: string;
  createdAt: string;
  subOrders?: VendorSubOrder[];
}

export interface Governorate {
  id: number;
  code: string;
  nameAr: string;
  nameFr: string;
}

export interface Delegation {
  id: number;
  governorateId: number;
  nameAr: string;
  nameFr: string;
}

export interface Locality {
  id: number;
  delegationId: number;
  nameAr: string;
  nameFr: string;
  postalCode: string;
}
