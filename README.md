# Sushi-preparation-and-delivery-system
A Java application that implements an inventory and delivery management system for a sushi business. 

This will keep track of stocks of ingredients and the sushi that has been prepared from these ingredients. It will also manage the fleet of drones for delivering sushi and restocking ingredients. The GUI for this application is provided by University of Southampton in order to demonstrate the ability to write a MVC pattern application.

The business and client applications run on the same machine (but in separate JVMs) and communication is achieved by making use of Socket communication.

The Server Application must be open when opening a Client instance.

Drones functionalities:
- monitor stock levels of ingredients
- when stock levels of ingredients drop below their restocking levels, it collects further ingredients from the appropriate supplier
- deliver customer orders

Staff functionalities:
- monitor the stock levels of dishes
- prepare a new dish when stock levels of ingredients drop below their restocking levels (using up the required ingredients) 

Both multiple kitchen staff and drones threads can operate concurrently.

Functionalities - Server Application:
- view current stock levels (of ingredients and dishes)
- change restocking levels
- change receipes of dishes
- add or remove ingredients, suppliers and dishes
- view the status of customer orders
- remove/cancel specific orders
- remove all completed orders
- view the status of kitchen staff and drones
- add or remove kitchen staff and drones
- load a configuration file
- back-up functionality (all data stored in the system is stored on disk in case the business application needs to be restarted at some point)

Functionalities - Client Application:
- new users are able to register with a username, password, an address, and a choice from a list of pre-defined postcodes that are served by the business
- existing users are able to log in with their previously chosen username and password
- show all available dishes
- add/remove dishes to/from a shopping basket, view the current total price and place their order
- see the status of current and previous orders
