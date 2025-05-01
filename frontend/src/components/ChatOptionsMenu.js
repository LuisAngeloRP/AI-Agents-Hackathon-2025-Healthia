import React, { useState, useEffect } from 'react';
import "../styles/ChatOptionsMenu.css";

// URL de la API desde variables de entorno
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000';

const ChatOptionsMenu = ({ isOpen, onClose, onNewChat, onSelectChat }) => {
    const [chats, setChats] = useState({
        today: [],
        yesterday: [],
        lastWeek: [],
        lastMonth: []
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (isOpen) {
            fetchChats();
        }
    }, [isOpen]);

    const fetchChats = async () => {
        try {
            setLoading(true);
            setError(null);
            console.log('📥 Solicitando lista de chats...');
            const response = await fetch(`${API_URL}/show-chats`, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'ngrok-skip-browser-warning': '69420'
                },
                mode: 'cors'
            });

            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }

            const data = await response.json();
            console.log('📊 Chats recibidos:', data);

            if (!data || !data.chats) {
                console.error('Estructura de datos inválida:', data);
                throw new Error('Formato de respuesta inválido');
            }

            // Verificar si hay chats
            if (data.chats.length === 0) {
                console.log('ℹ️ No hay conversaciones disponibles');
                setChats({ today: [], yesterday: [], lastWeek: [], lastMonth: [] });
                return;
            }

            const now = new Date();
            const sortedChats = data.chats.reduce((acc, chat) => {
                try {
                    const chatDate = new Date(chat.created_at);
                    const diffDays = Math.floor((now - chatDate) / (1000 * 60 * 60 * 24));

                    const chatInfo = {
                        id: chat.id,
                        title: chat.title || 'Chat sin título',
                        created_at: chat.created_at
                    };

                    if (diffDays === 0) {
                        acc.today.unshift(chatInfo);
                    } else if (diffDays === 1) {
                        acc.yesterday.unshift(chatInfo);
                    } else if (diffDays <= 7) {
                        acc.lastWeek.unshift(chatInfo);
                    } else if (diffDays <= 30) {
                        acc.lastMonth.unshift(chatInfo);
                    }

                    return acc;
                } catch (error) {
                    console.error('Error procesando chat:', chat, error);
                    return acc;
                }
            }, { today: [], yesterday: [], lastWeek: [], lastMonth: [] });

            console.log('Chats organizados:', sortedChats);
            setChats(sortedChats);

        } catch (error) {
            console.error('❌ Error al cargar chats:', error.message);
            setError(error.message);
            setChats({ today: [], yesterday: [], lastWeek: [], lastMonth: [] });
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteChat = async (chatId) => {
        try {
            console.log('🗑️ Intentando eliminar chat:', chatId);

            const response = await fetch(`${API_URL}/delete-chat/${chatId}`, {
                method: 'DELETE',
                headers: {
                    'Accept': 'application/json',
                    'ngrok-skip-browser-warning': '69420'
                }
            });

            if (!response.ok) {
                throw new Error(`Error ${response.status}`);
            }

            console.log('✅ Chat eliminado exitosamente:', chatId);
            fetchChats(); // Recargar la lista después de eliminar
        } catch (error) {
            console.error('❌ Error al eliminar chat:', error.message);
        }
    };

    const handleChatClick = (chatId) => {
        console.log('📱 Chat seleccionado:', chatId);
        onSelectChat?.(chatId); // Llamar a la función del padre si existe
        onClose(); // Cerrar el menú
    };

    const renderChatSection = (title, chatsArray) => {
        if (!chatsArray || chatsArray.length === 0) return null;

        return (
            <div className="chat-section">
                <h3 className="section-title">{title}</h3>
                {chatsArray.map(chat => (
                    <div 
                        key={chat.id} 
                        className="chat-item"
                        onClick={() => handleChatClick(chat.id)}
                    >
                        <span className="chat-title">{chat.title}</span>
                        <button 
                            className="delete-chat-button"
                            onClick={(e) => {
                                e.stopPropagation();
                                handleDeleteChat(chat.id);
                            }}
                        >
                            ×
                        </button>
                    </div>
                ))}
            </div>
        );
    };

    // Verificar si hay chats disponibles
    const hasChats = chats.today.length > 0 || chats.yesterday.length > 0 || 
                    chats.lastWeek.length > 0 || chats.lastMonth.length > 0;

    return (
        <>
            {isOpen && (
                <div className="menu-overlay" onClick={onClose}>
                    <div className="menu-container" onClick={e => e.stopPropagation()}>
                        <div className="menu-header">
                            <div className="menu-handle"></div>
                        </div>
                        <div className="menu-content">
                            <button 
                                className="menu-item new-chat" 
                                onClick={() => {
                                    onNewChat();
                                    onClose();
                                }}
                            >
                                <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M15.6729 3.91287C16.8918 2.69392 18.8682 2.69392 20.0871 3.91287C21.3061 5.13182 21.3061 7.10813 20.0871 8.32708L14.1499 14.2643C13.3849 15.0293 12.3925 15.5255 11.3215 15.6785L9.14142 15.9899C8.82983 16.0344 8.51546 15.9297 8.29289 15.7071C8.07033 15.4845 7.96554 15.1701 8.01005 14.8586L8.32149 12.6785C8.47449 11.6075 8.97072 10.615 9.7357 9.85006L15.6729 3.91287Z"/>
                                </svg>
                                Nuevo Chat
                            </button>
                            
                            {loading ? (
                                <div className="loading-message">Cargando conversaciones...</div>
                            ) : error ? (
                                <div className="error-message">Error al cargar conversaciones: {error}</div>
                            ) : !hasChats ? (
                                <div className="no-chats-message">No hay conversaciones disponibles</div>
                            ) : (
                                <>
                                    {renderChatSection('Today', chats.today)}
                                    {renderChatSection('Yesterday', chats.yesterday)}
                                    {renderChatSection('Last 7 days', chats.lastWeek)}
                                    {renderChatSection('Last 30 days', chats.lastMonth)}
                                </>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};

export default ChatOptionsMenu; 