// src/app/models/map-point.model.ts
export interface MapPoint {
  id: number;
  type: 'DEMANDE' | 'INTERVENTION';
  latitude: number;
  longitude: number;
  title: string;
  description: string;
  status: string;
  date: string;
  photos?: any[];
  color: string;
}
