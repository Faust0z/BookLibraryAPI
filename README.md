A Book Library REST API done in Spring Boot to manage users, books and loans in a modern and secure way.
The system implements:
- The Spring Security framework for security with a JWT implementation. Users receive a JWT token when reaching the auth/login endpoint with correct credentials. JWTs expire after 24hs.
- The correct Controller-Service-Repository design indicated by Spring.
- ModelMapper and DTOs to ensure that the requests and responses are done in a structured and DRY way.
- A GlobalExceptionHandler and custom exceptions for different bussiness rules.  

_Done in Spring 3.5.7, Java 21 and Maven with a local h2 database._


### Business rules
1. Can’t borrow a loan if no copies of the wanted book are available. When books are borrowed, their copies get reduced. When
   books are returned, their copies increase.
2. Each user has a limit of 3 active loans

Users can get their user info and loans by `/users/me` and `/loans/me` respectively, but they can also see the list of all users
and all loans with the base route since there are no roles (on porpuse for simplicity).

![database-schema](database-schema.png)
