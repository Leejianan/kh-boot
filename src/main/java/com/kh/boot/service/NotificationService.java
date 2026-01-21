package com.kh.boot.service;

/**
 * Interface for real-time notifications via WebSocket.
 */
public interface NotificationService {

    /**
     * Send kick-out notification to specific user
     * 
     * @param username User to kick
     * @param userType User type
     * @param token    The specific token to kick (optional, used for frontend
     *                 filtering)
     * @param reason   Reason message
     */
    void sendKickOut(String username, String userType, String token, String reason);

    /**
     * Broadcast an announcement to all online users.
     * 
     * @param content The content of the announcement
     */
    void broadcastAnnouncement(String content);
}
