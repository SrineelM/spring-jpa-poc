package com.example.demo.exception;

/**
 * Indicates the requested domain object does not exist (or is not visible to the current
 * principal). Keeping a dedicated type allows mapping to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType; // e.g. "User", "Post"
    private final Object resourceId; // identifier used in lookup (could be composite key part)

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message) {
        super(message); // custom message variant when id alone isn't enough
        this.resourceType = "Resource";
        this.resourceId = null;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Object getResourceId() {
        return resourceId;
    }
}
