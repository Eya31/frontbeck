import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemandeService } from '../../services/demande.service';
import * as L from 'leaflet';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-demande-form',
  standalone: true,
  imports: [CommonModule, FormsModule, LeafletModule, HttpClientModule],
  templateUrl: './demande-form.component.html',
  styleUrls: ['./demande-form.component.css']
})
export class DemandeFormComponent {
  step = 1;
  totalSteps = 5;
  isSubmitting = false;
  errorMessage: string | null = null;
  ticketId: string | null = null;

  categories = ['Voirie', 'Éclairage public', 'Eau & Assainissement', 'Déchets', 'Sécurité', 'Autre'];
  subCategories: { [key: string]: string[] } = {
    'Voirie': ['Nid-de-poule', 'Trottoir endommagé', 'Chaussée dégradée', 'Signalisation'],
    'Éclairage public': ['Lampadaire défectueux', 'Panne d\'éclairage', 'Câble apparent'],
    'Eau & Assainissement': ['Fuite d\'eau', 'Égout bouché', 'Débit faible', 'Odeur désagréable'],
    'Déchets': ['Poubelle pleine', 'Déchets abandonnés', 'Container endommagé'],
    'Sécurité': ['Trafic dangereux', 'Absence de passage piéton', 'Éclairage insuffisant'],
    'Autre': ['Autre problème']
  };

  currentSubCategories: string[] = [];

  priorites = [
    { label: 'Faible', value: 'LOW' },
    { label: 'Moyenne', value: 'MEDIUM' },
    { label: 'Haute', value: 'HIGH' }
  ];

  demande: any = {
    category: '',
    subCategory: '',
    description: '',
    priority: 'MEDIUM',
    // initialize to map center to avoid change-detection errors
    localisation: { latitude: 36.8065, longitude: 10.1815, address: '' },
    isAnonymous: false,
    contactEmail: '',
    dateSoumission: new Date().toISOString().split('T')[0]
  };

  selectedFiles: File[] = [];
  fileError: string | null = null;

  mapOptions: L.MapOptions = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
      })
    ],
    zoom: 12,
    center: L.latLng(36.8065, 10.1815) // Tunis coordinates
  };

  layers: L.Layer[] = [];
  private marker: L.Marker | null = null;
  map: L.Map | null = null;

  private redIcon = L.icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  constructor(private demandeService: DemandeService, private http: HttpClient) {}

  onMapReady(map: L.Map) {
    this.map = map;
    map.on('click', (e: L.LeafletMouseEvent) => {
      this.placeMarker(e.latlng);
    });

    // Set initial marker on Tunis if no location
    if (this.demande.localisation.latitude === 0) {
      const initialLatLng = L.latLng(36.8065, 10.1815);
      this.placeMarker(initialLatLng);
    }
  }

  private placeMarker(latlng: L.LatLng) {
    if (this.marker) {
      this.marker.setLatLng(latlng);
    } else {
      this.marker = L.marker(latlng, {
        icon: this.redIcon,
        draggable: true
      }).addTo(this.map!);

      this.marker.bindPopup("Emplacement du problème").openPopup();
      this.layers = [this.marker];

      this.marker.on('dragend', () => {
        this.updateLocationFromMarker();
      });
    }
    this.updateLocationFromMarker();
  }

  private updateLocationFromMarker() {
    if (this.marker) {
      const latlng = this.marker.getLatLng();
      this.demande.localisation.latitude = latlng.lat;
      this.demande.localisation.longitude = latlng.lng;
      this.getAddressFromLatLng(latlng);
    }
  }

  onCategoryChange() {
    this.currentSubCategories = this.subCategories[this.demande.category] || [];
    this.demande.subCategory = '';
  }

  // ✅ Reverse geocoding amélioré avec gestion d'erreur
  private getAddressFromLatLng(latlng: L.LatLng) {
    this.demande.localisation.address = 'Chargement de l\'adresse...';

    this.http.get<any>(`http://localhost:8080/api/demandes/reverse-geocode?lat=${latlng.lat}&lon=${latlng.lng}`)
      .subscribe({
        next: (data: any) => {
          if (data && data.display_name) {
            this.demande.localisation.address = data.display_name;
          } else {
            this.demande.localisation.address = `Position: ${latlng.lat.toFixed(6)}, ${latlng.lng.toFixed(6)}`;
          }
        },
        error: (err) => {
          console.warn('Erreur de reverse geocoding, utilisation de la position brute', err);
          this.demande.localisation.address = `Position: ${latlng.lat.toFixed(6)}, ${latlng.lng.toFixed(6)}`;
        }
      });
  }

  geolocate() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const latlng = L.latLng(pos.coords.latitude, pos.coords.longitude);
          this.demande.localisation.latitude = pos.coords.latitude;
          this.demande.localisation.longitude = pos.coords.longitude;

          if (this.map) {
            this.map.setView(latlng, 16);
            this.placeMarker(latlng);
          }
        },
        (error) => {
          console.error('Erreur de géolocalisation:', error);
          alert('Impossible de récupérer votre position. Vérifiez les permissions de localisation.');
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000
        }
      );
    } else {
      alert('Géolocalisation non supportée par votre navigateur.');
    }
  }

  nextStep() {
    if (this.validateStep(this.step)) {
      this.step++;
    } else {
      this.showStepError(this.step);
    }
  }

  prevStep() {
    if (this.step > 1) this.step--;
  }

  validateStep(step: number): boolean {
    switch(step) {
      case 1:
        return this.demande.localisation.latitude !== 0 &&
               this.demande.localisation.longitude !== 0;
      case 2:
        return !!this.demande.category;
      case 3:
        return (this.demande.description || '').length >= 10;
      case 4:
        return !this.fileError;
      default:
        return true;
    }
  }

  showStepError(step: number) {
    const errors = {
      1: 'Veuillez sélectionner un emplacement sur la carte',
      2: 'Veuillez sélectionner une catégorie',
      3: 'La description doit contenir au moins 10 caractères',
      4: this.fileError || 'Erreur avec les fichiers'
    };

    this.errorMessage = errors[step as keyof typeof errors];
    setTimeout(() => this.errorMessage = null, 5000);
  }

  onFileSelected(event: any) {
    this.fileError = null;
    this.selectedFiles = [];

    const files = event.target.files as FileList;
    if (!files || files.length === 0) return;

    if (files.length > 5) {
      this.fileError = 'Maximum 5 fichiers autorisés.';
      event.target.value = '';
      return;
    }

    const validFiles: File[] = [];
    for (let i = 0; i < files.length; i++) {
      const file = files.item(i)!;

      // Vérification taille
      if (file.size > 8 * 1024 * 1024) {
        this.fileError = `Le fichier "${file.name}" dépasse 8MB.`;
        continue;
      }

      // Vérification type
      const allowedTypes = [
        'image/jpeg', 'image/jpg', 'image/png', 'image/gif',
        'audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/mp3'
      ];

      if (!allowedTypes.includes(file.type)) {
        this.fileError = `Type de fichier non supporté: "${file.name}"`;
        continue;
      }

      validFiles.push(file);
    }

    this.selectedFiles = validFiles;

    if (validFiles.length === 0 && files.length > 0) {
      event.target.value = '';
    }
  }

  removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
  }

  submit() {
    // Validation finale
    if (this.demande.isAnonymous && !this.demande.contactEmail) {
      this.errorMessage = "L'email est obligatoire pour les signalements anonymes.";
      return;
    }

    if (!this.validateStep(1) || !this.validateStep(2) || !this.validateStep(3)) {
      this.errorMessage = "Veuillez compléter toutes les étapes obligatoires.";
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    // Préparation des données
    const demandeData = {
      ...this.demande,
      dateSoumission: new Date().toISOString().split('T')[0]
    };

    this.demandeService.createDemande(demandeData, this.selectedFiles)
      .subscribe({
        next: (res: any) => {
          this.ticketId = res.id ? 'SCTY-' + new Date().getFullYear() + '-' + String(res.id).padStart(6, '0') : null;
          this.step = this.totalSteps + 1;
          this.isSubmitting = false;
        },
        error: (err) => {
          console.error('Erreur création demande:', err);
          this.errorMessage = err.error?.error || err.error?.message || 'Une erreur est survenue lors de l\'envoi.';
          this.isSubmitting = false;
        }
      });
  }

  resetForm() {
    this.step = 1;
    this.ticketId = null;
    this.selectedFiles = [];
    this.errorMessage = null;
    this.demande = {
      category: '',
      subCategory: '',
      description: '',
      priority: 'MEDIUM',
      localisation: { latitude: 36.8065, longitude: 10.1815, address: '' },
      isAnonymous: false,
      contactEmail: '',
      dateSoumission: new Date().toISOString().split('T')[0]
    };

    // Reset map
    if (this.marker) {
      this.map?.removeLayer(this.marker);
      this.marker = null;
    }
  }

  getStepStatus(step: number): string {
    if (step < this.step) return 'completed';
    if (step === this.step) return 'active';
    return 'pending';
  }
}
