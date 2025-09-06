// Initialize map with default location (Seoul)
function initMap(containerId, options = {}) {
    const defaultOptions = {
        center: [37.5665, 126.9780], // Seoul
        zoom: 11,
        minZoom: 7,
        maxZoom: 18,
        zoomControl: true,
        ...options
    };

    // Initialize the map
    const map = L.map(containerId, {
        center: defaultOptions.center,
        zoom: defaultOptions.zoom,
        minZoom: defaultOptions.minZoom,
        maxZoom: defaultOptions.maxZoom,
        zoomControl: defaultOptions.zoomControl
    });

    // Add tile layer (OpenStreetMap)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    return map;
}

// Create a marker with popup
function createMarker(map, latlng, title, content, options = {}) {
    const defaultIcon = L.divIcon({
        className: 'custom-marker',
        html: '<div class="marker-pin"></div>',
        iconSize: [30, 42],
        iconAnchor: [15, 42],
        popupAnchor: [0, -30]
    });

    const marker = L.marker(latlng, {
        icon: defaultIcon,
        title: title,
        ...options
    }).addTo(map);

    if (content) {
        marker.bindPopup(content);
    }

    return marker;
}

// Create a marker for each day of the trip
function createDayMarkers(map, dailySchedules) {
    const dayMarkers = [];
    const dayColors = [
        '#FF5252', '#4CAF50', '#2196F3', '#9C27B0',
        '#FF9800', '#795548', '#607D8B', '#E91E63',
        '#00BCD4', '#8BC34A', '#FFC107', '#9E9E9E'
    ];

    dailySchedules.forEach((day, index) => {
        day.places.forEach(place => {
            if (place.latitude && place.longitude) {
                const color = dayColors[index % dayColors.length];
                const icon = L.divIcon({
                    className: 'custom-marker',
                    html: `<div class="marker-pin" style="background-color: ${color}">${index + 1}</div>`,
                    iconSize: [30, 42],
                    iconAnchor: [15, 42]
                });

                const marker = L.marker([place.latitude, place.longitude], {
                    icon: icon,
                    title: place.name
                }).addTo(map);

                marker.bindPopup(`
                    <div class="marker-popup">
                        <h5>Day ${index + 1}: ${place.name}</h5>
                        <p>${place.address || ''}</p>
                        ${place.time ? `<p><i class="fas fa-clock"></i> ${place.time}</p>` : ''}
                        ${place.description ? `<p>${place.description}</p>` : ''}
                    </div>
                `);

                dayMarkers.push(marker);
            }
        });
    });

    return dayMarkers;
}

// Create a route between markers
function createRoute(map, coordinates) {
    if (coordinates.length < 2) return null;

    const route = L.polyline(coordinates, {
        color: '#4285F4',
        weight: 4,
        opacity: 0.8,
        dashArray: '5, 5'
    }).addTo(map);

    return route;
}

// Fit map bounds to show all markers
function fitMapToMarkers(map, markers) {
    if (markers.length === 0) return;

    const group = new L.featureGroup(markers);
    map.fitBounds(group.getBounds().pad(0.1));
}

// Add click event to show marker on the map when clicking on a place in the itinerary
function setupItineraryItemClickHandler() {
    document.querySelectorAll('.itinerary-item').forEach(item => {
        item.addEventListener('click', function() {
            const lat = parseFloat(this.dataset.lat);
            const lng = parseFloat(this.dataset.lng);
            
            if (!isNaN(lat) && !isNaN(lng)) {
                // Remove any existing highlight
                document.querySelectorAll('.itinerary-item').forEach(i => {
                    i.classList.remove('active');
                });
                
                // Add highlight to clicked item
                this.classList.add('active');
                
                // Pan to the marker
                const map = window.tripMap; // Assuming map is stored in window
                map.setView([lat, lng], 15);
                
                // Open popup if available
                const marker = window.markers.find(m => 
                    m.getLatLng().lat === lat && m.getLatLng().lng === lng
                );
                
                if (marker) {
                    marker.openPopup();
                }
            }
        });
    });
}

// Initialize map with markers from the trip plan
function initTripMap(containerId, tripPlan) {
    const map = initMap(containerId);
    window.tripMap = map; // Store map in window for global access
    
    // Create markers for each day's places
    const markers = createDayMarkers(map, tripPlan.dailySchedules || []);
    window.markers = markers; // Store markers for later reference
    
    // Create routes if there are multiple points
    if (markers.length > 1) {
        const coordinates = markers.map(marker => [
            marker.getLatLng().lat,
            marker.getLatLng().lng
        ]);
        createRoute(map, coordinates);
    }
    
    // Fit map to show all markers
    if (markers.length > 0) {
        fitMapToMarkers(map, markers);
    }
    
    // Setup click handlers for itinerary items
    setupItineraryItemClickHandler();
    
    return map;
}

// Export functions for use in other files
window.MapUtils = {
    initMap,
    createMarker,
    createDayMarkers,
    createRoute,
    fitMapToMarkers,
    initTripMap
};
