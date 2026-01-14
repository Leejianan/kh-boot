# Frontend Integration Guide - RBAC System

## 1. Authentication
*   **Login API**: `POST /api/login`
*   **Token Storage**: Store the `token` from login response (e.g., in `localStorage`).
*   **Request Header**: All subsequent requests must carry:
    ```
    Authorization: Bearer <your_token>
    ```

## 2. User Info & Permissions (Button Control)
*   **Endpoint**: `GET /api/user/info`
*   **Purpose**: Call this to get user details and the list of permission keys (for buttons).
*   **Response**:
    ```json
    {
        "data": {
            "permissions": ["system:user:add", "system:user:edit"] // Use these for <Button v-auth>
        }
    }
    ```

## 3. Dynamic Menus (Sidebar Rendering)
*   **Endpoint**: `GET /api/user/menus`
*   **Purpose**: Call this to generate the sidebar menu. Returns a nested tree structure.
*   **Response Format**:
    ```json
    {
      "code": 200,
      "data": [
        {
          "name": "系统管理",
          "path": "/system",          // Directory Path
          "meta": { "title": "系统管理", "icon": "setting" },
          "children": [
             {
                "name": "用户管理",
                "path": "/system/user",   // Page Path
                "component": null,        // Can be used for dynamic component loading
                "meta": { "title": "用户管理" }
             }
          ]
        }
      ]
    }
    ```

## 4. Role Management (Admin)
*   **List Roles**: `GET /api/roles/page?pageNum=1&pageSize=10`
*   **Add Role**: `POST /api/roles` (Body: `{ "name": "HR", "roleKey": "hr", "sort": 1 }`)
*   **Update Role**: `PUT /api/roles` (Body: `{ "id": 1, "name": "HR" ... }`)
*   **Delete Role**: `DELETE /api/roles/{id}`
*   **Get Role Permissions**: `GET /api/roles/{id}/permissions` (Returns `[1, 2, 3]`)
*   **Assign Permissions**: `POST /api/roles/{id}/permissions` (Body: `[1, 2, 3]`)

## 5. Permission Tree (For Assigning)
*   **Endpoint**: `GET /api/permissions/tree`
*   **Purpose**: Returns full permission tree (including buttons) for the TreeSelect component in Role Dialog.

## 6. Permission Keys Dictionary
Use these keys to control UI element visibility (Buttons).

| Key | Description | Type |
| :--- | :--- | :--- |
| `system:user:list` | View User Management Page | Menu |
| `system:user:add` | Add User Button | Button |
| `system:user:edit` | Edit User Button | Button |
| `system:user:delete` | Delete User Button | Button |
| `system:role:list` | View Role Management Page | Menu |
| `system:role:add` | Add Role Button | Button |
| `system:role:assign` | Assign Permissions Button | Button |
| `item:list` | View Item List Page | Menu |
| `item:add` | Add Item Button | Button |
| `item:edit` | Edit Item Button | Button |
| `item:delete` | Delete Item Button | Button |
| `item-types:tree` | View Item Type Tree | Menu |
| `item-types:list` | View Item Type List | Button |
| `item-types:add` | Add Item Type Button | Button |
| `item-types:edit` | Edit Item Type Button | Button |
| `item-types:delete` | Delete Item Type Button | Button |

## 7. Error Handling
*   **Http 403 Forbidden**: Indicates the user's capabilities do not allow this action. Frontend should catch this global error and show a "Permission Denied" toast or redirect.
