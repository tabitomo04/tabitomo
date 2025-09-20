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

    const map = L.map(containerId, {
        center: defaultOptions.center,
        zoom: defaultOptions.zoom,
        minZoom: defaultOptions.minZoom,
        maxZoom: defaultOptions.maxZoom,
        zoomControl: defaultOptions.zoomControl
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    return map;
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
        if (day.schedules) {
            day.schedules.forEach(schedule => {
                if (schedule.latitude && schedule.longitude) {
                    const color = dayColors[index % dayColors.length];
                    const icon = L.divIcon({
                        className: 'custom-marker',
                        html: `<div class="marker-pin" style="background-color: ${color}">${index + 1}</div>`,
                        iconSize: [30, 42],
                        iconAnchor: [15, 42]
                    });

                    const marker = L.marker([schedule.latitude, schedule.longitude], {
                        icon: icon,
                        title: schedule.place
                    }).addTo(map);

                    let timeString = '';
                    if (schedule.startTime) {
                        timeString = schedule.startTime;
                        if (schedule.endTime) {
                            timeString += ` - ${schedule.endTime}`;
                        }
                    }

                    marker.bindPopup(`
                        <div class="marker-popup">
                            <h5>Day ${index + 1}: ${schedule.place}</h5>
                            ${timeString ? `<p><i class="fas fa-clock"></i> ${timeString}</p>` : ''}
                            ${schedule.description ? `<p>${schedule.description}</p>` : ''}
                        </div>
                    `);

                    dayMarkers.push(marker);
                }
            });
        }
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
function setupScheduleItemClickHandler() {
    document.querySelectorAll('.itinerary-item').forEach(item => {
        item.addEventListener('click', function() {
            const lat = parseFloat(this.dataset.lat);
            const lng = parseFloat(this.dataset.lng);
            
            if (!isNaN(lat) && !isNaN(lng)) {
                document.querySelectorAll('.itinerary-item').forEach(i => i.classList.remove('active'));
                this.classList.add('active');
                
                const map = window.tripMap;
                map.setView([lat, lng], 15);
                
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
    window.tripMap = map;
    
    const markers = createDayMarkers(map, tripPlan.dailySchedules || []);
    window.markers = markers;
    
    if (markers.length > 1) {
        const coordinates = markers.map(marker => [
            marker.getLatLng().lat,
            marker.getLatLng().lng
        ]);
        createRoute(map, coordinates);
    }
    
    if (markers.length > 0) {
        fitMapToMarkers(map, markers);
    }
    
    setupScheduleItemClickHandler();
    
    return map;
}

// Export functions for use in other files
window.MapUtils = {
    initMap,
    createDayMarkers,
    createRoute,
    fitMapToMarkers,
    initTripMap
};
