# SimpleWorldMap
This is a Simple World Map application which enables to search a place and get marker on that place

## Features
- MapActivity: Shows Map and points Marker depending on the search done
- SearchActivity: Allows you to search any location with autocomplete features
- Supports Portrait and Landscape modes

## Technology Deep Dive
### Android specifics
- Minimum sdk version is 21 and target sdk version is 26
- Uses permission ACCESS_FINE_LOCATION and INTERNET
- Application will ask for internet permission if Wifi or data pack both are disabled

### Maps and Places
- Map is rendered using SupportMapFragment from com.google.android.gms.maps
- 2 external dependencies have been used: Google Play Services Maps and Google Play Services Places
- GoogleMap and OnMapReadyCallBack are used to render Map in Support Fragment and both are parts of Google Play Services Maps
- 2 Google Place APIs have been used: getPlaceId and getAutoCompletePredictions which are both part of Places class from com.google.android.gms.location.places

### Custom Controls
- In Search Activity, the recycler view and each item of recycler view is customized 
- For rendering data in recycler view, PlaceAutocompleteAdapter has been created which uses Filterable interface
- In both the activities, Search EditText has CardView in its background layout

### Application Flow
- Consists of 2 Activities: MapActivity and SearchActivity
- MapActivity: Click on the Search EditText to enter a search location. Activity takes the marker to that location (with camera animation)
- MapActivity: On click of Search Box, SearchActivity with RecyclerView comes up
- MapActivity: On clicking 'X', will clear the text and the Marker showing on the Map
- SearchActivity: On entering characters, you will find search results
- SearchActivity: On selection of the place, it will take you to the main MapActivity and point the marker to its closest possible zoom level
- SearchActivity: Clicking 'X' will clear the text in the search box and finally to the MapActivity

## Additional Features
- You can click on the marker and see a tooltip saying which place is it marking
- On cliking the marker, it changes the color depicting that it has been clicked and shows the tooltip/title on top of it
- Click on the 'earth icon' to change the style of the Map from Retro, Night, Grayscale and Default views. Some styles help to view roads and places very clearly
- Earth icon and cancel/clear icons have alpha animation defined on them for showing click effect
