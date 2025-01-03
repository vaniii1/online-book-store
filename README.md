﻿# online book store 📚

<style>
  h2 {
    text-align: center;
  }
</style>

<h2>Description: ✍️</h2>

My online book store is an application based on Spring Boot technology. It implements the main logic of any online
store with shopping cart and orders, and has more than 20 endpoints with an ability to register and login. You can
try it yourself with <a href=http://ec2-3-89-225-196.compute-1.amazonaws.com/api/swagger-ui/index.html#/> this swagger link!</a>
If you have any difficulties you can watch <a href="https://www.loom.com/share/688287d302c241f7b373fe2901c85b2c?sid=4f1947af-4725-4865-86e4-e06d9f8fd29e">My Video Tutorial! </a>

<h2>Features: ✨</h2>

- Register and login system
- Create new category, book, cart item, order
- Display book, category, shopping cart, order
- Update book, category, cart item quantity, order status
- Remove book, category, cart item

<h2>How to use the application: 🔍</h2>

When you follow <a href=http://ec2-3-89-225-196.compute-1.amazonaws.com/api/swagger-ui/index.html#/> the link </a>,
two endpoints `/api/login` and `/api/register` are available to you. If you want to use any other endpoints, it is obligatory
to login first. The user whose username contains `admin@` obtains _ROLE_ADMIN_ automatically and so is capable to __create,
update and delete__ books and categories. Also, it is possible for him to __update__ the status of the order.
There are four order status:
```
public enum Status {
   COMPLETED,
   PENDING,
   DELIVERED,
   ON_THE_WAY
}
```

When you log in, the __bearer (authentication) token__ that is formed of three key components:
the header, the payload and the signature, appears in front of you. It is important to __copy it__ because we need to use generated
token in our future authentications to other endpoints.

<h2>Technologies: 🕹️</h2>
- __Spring Boot__ (tool helps to develop web application) <br>
- __Spring Boot JPA__ (interface provides with annotations and classes to help with the Repository layer) <br>
- __Lombok__ (library provides with a set of annotations that help with managing Entities) <br>
- __Liquibase__ (convenient library for managing databases) <br>
- __JWT__ (web token that shares information between a client and a server) <br>
- __Spring security__ (framework that helps with authentication and user access control) <br>
- __Mapstruct__ (library helps to create a mapper implementation) <br>
- __JUnit__ (testing framework for java) <br>
- __Pagination__ (provides with ability to divide a page into smaller parts) <br>
- __Swagger__ (provides with a set of annotations that help to describe controllers` functionality) <br>
- __MySQL__ (database that was used in the project) <br>
- __Hibernate__ (framework provides with a bunch of functions that help to save and build relationships between Entities in database) <br>
- __Docker__ (platform for running and sharing applications between developers) <br>
- __Maven__ (necessary management tool for Java projects) <br>
- __Git__ (version control system that helps to track changes in your project) <br>
- __AWS__ (cloud computing service allows you to deploy your application) : <br>
    - EC2 (provides with compute capacity in the cloud) <br>
    - RDS (provides with a database in the cloud) <br>
    - IAM (helps to control access to resources) <br>
    - ECR (provides the repository of the project) <br>

<h2>Setting up on your computer: 📲</h2>

1. <h3>Required: </h3>
    - Java 17
    - Docker
    - MySQL
    - Maven

2. When you finished downloading required services you need to set your environment variables in _.env_ file. There is
You can find a template file _(.env.template)_ about how it should look like.
<br>
_.env_ file: 
```
MYSQLDB_USER=
MYSQLDB_ROOT_PASSWORD=
MYSQLDB_DATABASE=
MYSQLDB_LOCAL_PORT=
MYSQLDB_DOCKER_PORT=
SPRING_LOCAL_PORT=
SPRING_DOCKER_PORT=
DEBUG_PORT=
```
_.env.template_ file:
```
MYSQLDB_USER=root
MYSQLDB_ROOT_PASSWORD=1234
MYSQLDB_DATABASE=bookstore
MYSQLDB_LOCAL_PORT=3303
MYSQLDB_DOCKER_PORT=3306
SPRING_LOCAL_PORT=4040
SPRING_DOCKER_PORT=8080
DEBUG_PORT=5005
```

You need to connect MySQL database to this project and set values, such as password, user, and database name in _.env_ file.
The port that is going to be used for your endpoints will be stored in `SPRING_LOCAL_PORT=` field. For example, if
`SPRING_LOCAL_PORT=` has value `4040`, your endpoint will have a look like this: `http://localhost:4040/api/..`

4. After you set up the environment variables, it is important to start Docker Desktop 
software and after run the following commands in the terminal:
   1. `mvn clean package` (to build the .jar file)
   2. `docker-compose up` (to build the container for the application)

<h2>Tips on endpoints: 📌</h2>

Totally, there are 23 endpoints with different functions. <br>
__Context path:__ /api
<table>
    <tr>
        <td><b><i>Method</i></b></td>
        <td><b><i>Url</i></b></td>
        <td><b><i>Action</i></b></td>
        <td><b><i>Required fields to fill</i></b></td>
        <td><b><i>Optional fields to fill</i></b></td>
        <td><b><i>Required Authorities</i></b></td>
    </tr>
    <tr>
        <td>POST</td>
        <td><i>/register</i></td>
        <td>Register a new user</td>
        <td>'email', 'firstName', 'lastName', 'password', 'repeatPassword'</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>        
    </tr>
    <tr>
        <td>POST</td>
        <td><i>/login</i></td>
        <td>Log in and receive an authentication token</td>
        <td>'email', 'password'</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
    </tr>
    <tr>
        <td>POST</td>
        <td><i>/categories</i></td>
        <td>Create a new category</td>
        <td>'name'</td>
        <td>'description'</td>
        <td><i>ROLE_ADMIN</i></td>
    </tr> 
    <tr>
        <td>GET</td>
        <td><i>/categories</i></td>
        <td>Get all categories</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>GET</td>
        <td><i>/categories/{category_id}</i></td>
        <td>Get the category by id</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>PUT</td>
        <td><i>/categories/{category_id}</i></td>
        <td>Update the category by id</td>
        <td>'name'</td>
        <td>'description'</td>
        <td><i>ROLE_ADMIN</i></td>
    </tr>
    <tr>
        <td>DELETE</td>
        <td><i>/categories/{category_id}</i></td>
        <td>Delete the category by id</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_ADMIN</i></td>
    </tr>
    <tr>
        <td>GET</td>
        <td><i>/categories/{category_id}/books</i></td>
        <td>Get books with certain category</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>POST</td>
        <td><i>/books</i></td>
        <td>Create a new book</td>
        <td>'author', 'title', 'isbn', 'price', 'categoryIds'</td>
        <td>'description', 'coverImage'</td>
        <td><i>ROLE_ADMIN</i></td>
    </tr>
    <tr>
        <td>GET</td>
        <td><i>/books</i></td>
        <td>Get all books</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>GET</td>
        <td><i>/books/{book_id}</i></td>
        <td>Get the book by id</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>PUT</td>
        <td><i>/books/{book_id}</i></td>
        <td>Update the book by id</td>
        <td>'author', 'title', 'isbn', 'price', 'categoryIds'</td>
        <td>'description', 'coverImage'</td>
        <td><i>ROLE_ADMIN</i></td>
    </tr>
    <tr>
        <td>DELETE</td>
        <td><i>/books/{book_id}</i></td>
        <td>Delete the book by id</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_ADMIN</i></td>
    </tr>
    <tr>
        <td>GET</td>
        <td><i>/books/search{?param=value}</i></td>
        <td>Get all books with a certain param</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>POST</td>
        <td><i>/cart</i></td>
        <td>Add a new book to the shopping cart</td>
        <td>'bookId', 'quantity'</td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>GET</td>
        <td><i>/cart</i></td>
        <td>Get the cart of the current user</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>PUT</td>
        <td><i>/cart/cart-items/{cart_item_id}</i></td>
        <td>Update the cart item quantity</td>
        <td>'quantity'</td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>DELETE</td>
        <td><i>/cart/cart-items/{cart_item_id}</i></td>
        <td>Delete the cart item from the shopping cart</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>POST</td>
        <td><i>/orders</i></td>
        <td>Create a new order</td>
        <td>'shippingAddress'</td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>GET</td>
        <td><i>/orders</i></td>
        <td>Get all orders of the current user</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>PATCH</td>
        <td><i>/orders/{order_id}</i></td>
        <td>Update a status of the order</td>
        <td>'status'</td>
        <td><i>NONE</i></td>
        <td><i>ROLE_ADMIN</i></td>
    </tr>
    <tr>
        <td>GET</td>
        <td><i>/orders/{order_id}/items</i></td>
        <td>Get all items of the order</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
    <tr>
        <td>GET</td>
        <td><i>/orders/{order_id}/items/{item_id}</i></td>
        <td>Get the certain item of the certain order</td>
        <td><i>NONE</i></td>
        <td><i>NONE</i></td>
        <td><i>ROLE_USER</i></td>
    </tr>
</table>

<h2>Examples of usage in Swagger: </h2>
<h3>Requests: 📥
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Responses: 📤</h3>
<h3>Registration </h3>
<img src="pictures/registration-request.png"><img src="pictures/registration-response.png">

<h3>Create book </h3>
<img src="pictures/create-book-request.png"><img src="pictures/create-book-response.png">

<h3>Create order </h3>
<img src="pictures/create-order-request.png"><img src="pictures/create-order-response.png">

<h2>Test coverage: ❇️ </h2>
<h3>Tests cover more than 90% of methods in the project</h3>
<img src="pictures/book-store-test-coverage.png">

<h2>>Database diagram: 📒</h2>
<img src="pictures/book-store-diagram.png">
