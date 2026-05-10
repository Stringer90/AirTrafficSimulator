# Air Traffic Simulation

Air Traffic Simulator is a Java desktop application that simulates aircraft movement between airports across a 2D map. The project demonstrates multithreading, concurrency, GUI programming, event-driven systems, and real-time simulation updates using JavaFX.

## Features

- Randomly generated airports
- Multiple planes assigned to each airport
- Real-time aircraft movement simulation
- Flight request handling using external communication classes
- Plane servicing simulation after landing
- Live statistics and event logging
- Responsive GUI with continuous updates
- Graceful simulation start and shutdown handling

## Simulation Overview

The simulation takes place on a rectangular map where:

- Airports are placed randomly with unique IDs
- Planes travel between airports in straight lines
- Aircraft move at a constant speed
- Flight requests are generated asynchronously
- Planes undergo servicing after landing
- Multiple aircraft can fly and be serviced simultaneously

## Technologies Used

- Java
- JavaFX
- Multithreading & Concurrency
- Synchronization & Shared State Management
- Event Handling
- Object-Oriented Design

## Core Components

### Airports

- Fixed positions on the map
- Unique airport IDs
- Handle incoming and outgoing flight requests

### Planes

- Unique plane IDs
- Move continuously across the map
- Can be:
  - Idle
  - In-flight
  - Undergoing servicing

### Flight Requests

The simulation integrates with provided communication classes:

- `StandardFlightRequests`
- `StandardPlaneServicing`

Flight requests are generated asynchronously and processed using separate threads.

## GUI Features

- Real-time plane position updates
- Airport and plane captions with IDs
- Live event log for departures and landings
- Dynamic simulation statistics:
  - Planes in-flight
  - Planes under servicing
  - Completed trips
- Smooth and responsive interface

## Simulation Controls

### Start Button

- Begins the simulation
- Starts flight request generation
- Activates aircraft movement

### End Button

- Gracefully stops the simulation
- Freezes all aircraft in their current positions
- Preserves all visible simulation data

### Window Close

- Properly shuts down all running threads
- Safely exits the program

## Multithreading Concepts

This project demonstrates:

- Concurrent flight request handling
- Independent aircraft servicing
- Thread-safe shared data access
- Real-time GUI updates without freezing
- Graceful thread termination

## Learning Outcomes

This project demonstrates understanding of:

- Java concurrency and synchronization
- GUI development with JavaFX/Swing
- Real-time simulation systems
- Producer-consumer style architectures
- Thread lifecycle management
- Object-oriented software design

## Assignment Information

**Unit:** COMP3003 Software Architecture and Extensible Design 

**Assignment:** Assignment 1

**Semester:** 2025, Semester 2

<p align="center">
  <img width="1353" height="1027" alt="Screenshot 2026-05-10 160247" src="https://github.com/user-attachments/assets/e6ca47a9-9581-49a0-a5b9-5d09a6101247" />
</p>
