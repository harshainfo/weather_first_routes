# WeatherFirstRoutes

## Inspiration based on a true story
Myself(Harsha) and my wife attended a wedding in Chicago and we originally had booked an Airbnb for us to say since wedding was supposed to go until 10pm. However last moment we cancelled that and decided that we will drive home to Iowa City same night, to save the next day and also bit of money on lodging. We left Chicago at 9.30pm and as soon as we put on to I-380, the road was entirely covered with fog. We were very worried as it was our first experience and thought we better had stayed at Chicagio that night. This lead us to think why do travelers always take either shortest distance or shortest travel time routes while a safer option in relation to weather conditions is available. We believe you may also have come across enough such incidents in life. 
Additionally according to the Federal Highway Administration (FHWA) out of nearly six million vehicular accidents that occur every year in the U.S., approximately 22 % are weather-related. Nearly **6,000** people are **killed** and another **445,000 injured** due to accidents that occur during bad weather.​
_ https://www.blumenshinelawgroup.com/how-bad-weather-increases-driving-accidents/​ _
However surprisingly many of the popular navigation systems have yet failed to incorporate the important aspect of weather into their navigation systems. Our WeatherFirstRoutes is an eye opener for many of them.

## What it does
Once you specify an origin and a destination, it gives the same number of routes as google directions would give. Additionally it displays weather icons to indicate current weather conditions along the routes. After getting an idea about the weather conditions in alternative routes, traveller can select the best route on google maps to navigate. So the traveller can avoid extreme weather conditions such as snow, thunderstorms or delay the journey until the weather turns good rather than taking unnecessary risk.  

## How we built it
We used Android studio in the development. We used **google maps, directions, places cloud products/APIs and Openweathermap API**. Openweathermap provided weather predictions and relevant icons while routes were obtained from directions API. To specify origin and destination, places API was used with auto complete fragments. Maps api was used to display map, routes(through polylines) and weather icons. This works with any android device with Oreo 8.0.0/API 26 or later.

## Challenges we ran into
Google does not allow similar products to be made using their APIs. So we cannot build a navigator. Additionally the no of steps/waypoints in direction routes are also limited so we cannot get a perfect smooth route as original google maps. However our polyline is capable to alert the traveller about extreme weather conditions prevalent in the areas around alternative routes.

## Accomplishments that we are proud of
Our app is capable of what it was supposed to do: give user the safest route option ion terms of weather. We were able to integrate four external cloud/API technologies from which 3 are google. We made use of the Google Student Cloud Platform credit we got from HackISU in Spring 2018. We came to Spring 2018 HackISU and HackUIowa Fall 2018, in neither we could demo our product. But this time we made it. This is our first submission and demo at a MLH Hackathon!!!!!!!!

## What we learned
We learnt how to call external APIs, how to process their xml responses, how to do debugging, how to integrate google's important auto complete fragments.

## What's next for WeatherFirstRoutes
We have a business model for WeatherFirstRoutes for sustainability. We plan to integrate that with other service provider APIs such as Starbucks, Airbnb and Tripadvisor which could help travelers to decide where to have a coffee or have some rest until weather turns goods. This will give us the opportunity to market brands such as Starbucks, Airbnb and Tripadvisor, and be benefitted in return.
