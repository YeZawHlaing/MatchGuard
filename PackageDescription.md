## Package Descriptions


| Package Name | Responsibility & Contents |
|:-------------|:--------------------------|
| config       |          Houses system-wide configuration beans, such as CORS mappings, RestTemplate/WebClient configurations, and security beans.                 |
| controller   |      Entry points for API requests. Maps HTTP verbs (`GET, POST, etc.`) to service methods and handles routing for Auth, Products, and Transactions.                     |
| service      |     The heart of the application. Encapsulates business rules, validation workflows, and orchestrates calls to external AI LLM APIs and ZXing utility libraries.                      |
| repository   |         Interfaces extending JpaRepository. Provides CRUD operations and custom query methods (`findByEmail, findByQrToken,` etc.) connected to PostgreSQL.                  |
| entity       |       Contains database-mapped entity classes (`User, Product, Transaction`) annotated with JPA/Hibernate annotations, along with associated Enums.                    |
| dto          |       Plain Old Java Objects (POJOs) used to transfer data strictly between the client and controller, decoupling internal database entities from API contracts.                    |
| securty      |          Implements Spring Security filters, custom user detail loading, and JWT token generation/validation logic for stateless mobile requests.                 |
| exception    |      Centralized exception handling using` @ControllerAdvice` to intercept application errors and return standardized JSON error responses.                     |

---
