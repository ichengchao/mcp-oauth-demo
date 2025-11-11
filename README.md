# MCP Java SDK Demo

A Spring Boot implementation of MCP (Model Context Protocol) server with OAuth 2.0 authentication and salary query tools.

## Features

- **OAuth 2.0 Server** - Complete authorization code flow with PKCE support
- **Dynamic Client Registration** - RFC 7591 compliant
- **Salary Query Tools** - MCP tools for querying employee salary information
- **Mock Data** - 12 months of salary data for 3 employees

## Quick Start

### Run the Application

```bash
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

### Test Users

| Username | Password | Employee Name    | Employee ID |
|----------|----------|------------------|-------------|
| alice    | 123      | Alice Wang       | EMP001      |
| bob      | 123      | Bob Li           | EMP002      |
| charles  | 123      | Charles Zhang    | EMP003      |

## OAuth 2.0 Endpoints

- **Authorization Metadata**: `GET /.well-known/oauth-authorization-server`
- **Register Client**: `POST /oauth/register`
- **Authorize**: `GET /oauth/authorize`
- **Token**: `POST /oauth/token`
- **JWKS**: `GET /oauth/keys`
- **List Clients**: `GET /oauth/clients` (debug endpoint)

## MCP Tools

### 1. getMySalary

Query current month's salary information.

```
Returns: Salary details including base salary, bonus, and total salary
```

### 2. getMySalaryByMonth

Query salary information for a specific month.

```
Parameter: month (format: YYYY-MM, e.g., 2025-01)
Returns: Salary details for the specified month
```

### 3. Calculator Tools

Basic arithmetic operations: add, subtract, multiply, divide

## Testing with MCP Inspector

1. Start the application
2. Configure MCP Inspector to use `http://localhost:8080`
3. Complete OAuth flow using any test user
4. Call the salary query tools

## Mock Data

Each employee has 12 months of salary data (2025-01 to 2025-12):

- **Alice Wang (EMP001)**: Base salary ¥15,000, bonus ~¥3,000 (varies by month)
- **Bob Li (EMP002)**: Base salary ¥18,000, bonus ~¥4,500 (varies by month)
- **Charles Zhang (EMP003)**: Base salary ¥20,000, bonus ~¥5,000 (varies by month)

## Access Control

Each user can only query their own salary information. The employee ID is extracted from the OAuth access token.
