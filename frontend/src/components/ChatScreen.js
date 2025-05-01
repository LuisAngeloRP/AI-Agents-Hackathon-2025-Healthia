import React, { useState, useEffect, useRef } from 'react';
import "../styles/ChatScreen.css";
import "../styles/SharedContainer.css";
import logoDashboard from "../assets/logos/logo_sign.png";
import clipIcon from "../assets/icons/clip.svg";
import micIcon from "../assets/icons/mic.svg";
import cameraIcon from "../assets/icons/camera.svg";
import ChatOptionsMenu from './ChatOptionsMenu';

/* ------------------------------------------------------------------
   Funciones helper para enviar requests a tu backend (FastAPI)
   ------------------------------------------------------------------ */

// URL de la API desde variables de entorno
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000';

// 1. Función para enviar texto al chatbot
const enviarTexto = async (mensaje, conversationId) => {
  const formData = new FormData();

  // Campos requeridos
  formData.append('message', mensaje);
  formData.append('id', conversationId);
  formData.append('type', 'text');

  try {
    const response = await fetch(`${API_URL}/chatbot`, {
      method: 'PUT',
      body: formData,
      headers: {
        'Accept': 'application/json',
        'ngrok-skip-browser-warning': '69420'
      }
    });

    if (!response.ok) {
      throw new Error(`Error HTTP: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al enviar el texto:', error.message);
    throw error;
  }
};

// 2. Función para enviar una imagen al chatbot
const enviarImagen = async (mensaje, conversationId, imageFile) => {
  const formData = new FormData();

  // Campos requeridos
  formData.append('message', mensaje);
  formData.append('id', conversationId);
  formData.append('type', 'image');
  
  // Agregar el archivo de imagen
  formData.append('media_file', imageFile);

  try {
    const response = await fetch(`${API_URL}/chatbot`, {
      method: 'PUT',
      body: formData,
      headers: {
        'Accept': 'application/json',
        'ngrok-skip-browser-warning': '69420'
      }
    });

    if (!response.ok) {
      throw new Error(`Error HTTP: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al enviar la imagen:', error.message);
    throw error;
  }
};

// 3. Función para enviar un audio al chatbot
const enviarAudio = async (mensaje, conversationId, audioFile) => {
  const formData = new FormData();

  // Campos requeridos
  formData.append('message', mensaje);
  formData.append('id', conversationId);
  formData.append('type', 'audio');

  // Agregar el archivo de audio
  formData.append('media_file', audioFile);

  try {
    const response = await fetch(`${API_URL}/chatbot`, {
      method: 'PUT',
      body: formData,
      headers: {
        'Accept': 'application/json',
        'ngrok-skip-browser-warning': '69420'
      }
    });

    if (!response.ok) {
      throw new Error(`Error HTTP: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al enviar el audio:', error.message);
    throw error;
  }
};

/* ------------------------------------------------------------------
   Componente principal ChatScreen
   ------------------------------------------------------------------ */
const ChatScreen = ({ onBack }) => {
  const [chatId, setChatId] = useState(1);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef(null);
  const [isRecording, setIsRecording] = useState(false);
  const [mediaRecorder, setMediaRecorder] = useState(null);
  const audioChunks = useRef([]);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  
  // Estados para la previsualización de imagen (modificado)
  const [selectedImage, setSelectedImage] = useState(null);
  const [imagePreviewUrl, setImagePreviewUrl] = useState('');
  const [showImagePreview, setShowImagePreview] = useState(false);

  useEffect(() => {
    // Ajusta la meta viewport
    const meta = document.createElement('meta');
    meta.name = 'viewport';
    meta.content = 'width=device-width, initial-scale=1, maximum-scale=1, viewport-fit=cover, user-scalable=no';
    document.head.appendChild(meta);

    return () => {
      document.head.removeChild(meta);
    };
  }, []);

  useEffect(() => {
    // Prevenir scroll del body
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = '';
    };
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Ajustar el espacio en el área de mensajes cuando la previsualización está activa
  useEffect(() => {
    const messagesContainer = document.querySelector('.messages-container');
    if (messagesContainer) {
      if (showImagePreview) {
        messagesContainer.style.marginBottom = '90px'; // Ajustado para la nueva altura
      } else {
        messagesContainer.style.marginBottom = '70px'; // Valor original
      }
      scrollToBottom();
    }
  }, [showImagePreview]);

  const scrollToBottom = () => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }
  };

  // Mensaje de bienvenida local
  useEffect(() => {
    setMessages([{
      type: 'bot',
      content: '¡Hola! Soy HealthIA, tu asistente personal de salud. ¿En qué puedo ayudarte hoy?'
    }]);
  }, []);

  // Inicializar el chatId al cargar el componente
  useEffect(() => {
    const initializeChatId = async () => {
      try {
        // Obtener la lista de chats existentes
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
        
        // Encontrar el ID más alto
        let maxId = 0;
        if (data && data.chats && data.chats.length > 0) {
          maxId = Math.max(...data.chats.map(chat => chat.id));
          console.log('🔢 IDs de chats existentes al iniciar:', data.chats.map(chat => chat.id));
          console.log('🔢 ID más alto encontrado:', maxId);
          // Usar el ID más alto + 1 para el nuevo chat
          setChatId(maxId + 1);
        } else {
          console.log('ℹ️ No hay chats previos, comenzando desde ID 1');
          setChatId(1);
        }
      } catch (error) {
        console.error('❌ Error al inicializar chatId:', error.message);
        // Si hay un error, usar ID 1
        setChatId(1);
      }
    };

    initializeChatId();
  }, []);

  // Iniciar un nuevo chat (id = máximo id existente + 1) y limpiar mensajes
  const handleNewChat = async () => {
    try {
      console.log('🔄 Obteniendo lista de chats para crear uno nuevo...');
      
      // Obtener la lista de chats existentes
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
      
      // Encontrar el ID más alto
      let maxId = 0;
      if (data && data.chats && data.chats.length > 0) {
        maxId = Math.max(...data.chats.map(chat => chat.id));
        console.log('🔢 IDs de chats existentes:', data.chats.map(chat => chat.id));
      } else {
        console.log('ℹ️ No hay chats previos, comenzando desde ID 1');
      }
      
      // Usar el ID más alto + 1 para el nuevo chat
      const newChatId = maxId + 1;
      
      console.log('🔄 Iniciando nuevo chat:', {
        maxIdExistente: maxId,
        nuevoChatId: newChatId,
        timestamp: new Date().toISOString()
      });
      
      setChatId(newChatId);
      setMessages([{
        type: 'bot',
        content: '¡Hola! Soy HealthIA, tu asistente personal de salud. ¿En qué puedo ayudarte hoy?'
      }]);
    } catch (error) {
      console.error('❌ Error al crear nuevo chat:', error.message);
      // Si hay un error, usar un ID incrementado localmente
      const newChatId = chatId + 1;
      console.log('⚠️ Usando ID local incrementado:', newChatId);
      
      setChatId(newChatId);
      setMessages([{
        type: 'bot',
        content: '¡Hola! Soy HealthIA, tu asistente personal de salud. ¿En qué puedo ayudarte hoy?'
      }]);
    }
  };

  // Enviar mensaje (modificado para manejar también imágenes)
  const handleSendMessage = async (e) => {
    e.preventDefault();
    
    // Si hay una imagen seleccionada, enviarla con la instrucción
    if (showImagePreview && selectedImage) {
      const instruction = inputMessage.trim() || 'Analiza esta imagen';
      
      console.log('🟦 Usuario envía imagen con instrucción:', {
        instruccion: instruction,
        nombre: selectedImage.name,
        tipo: selectedImage.type,
        tamaño: `${(selectedImage.size / 1024).toFixed(2)}KB`,
        chatId: chatId,
        timestamp: new Date().toISOString()
      });

      // Mostrar mensaje en la interfaz
      setMessages(prev => [...prev, {
        type: 'user',
        content: instruction,
        image: imagePreviewUrl
      }]);
      
      // Ocultar la previsualización y limpiar el input
      setShowImagePreview(false);
      setInputMessage('');
      setIsTyping(true);

      try {
        // Enviar al backend
        const data = await enviarImagen(instruction, chatId, selectedImage);
        // Respuesta del bot
        const botResponse = {
          type: 'bot',
          content: data.respuesta || 'No hay respuesta'
        };
        setMessages(prev => [...prev, botResponse]);
      } catch (error) {
        console.error('Error al enviar imagen:', error);
        setMessages(prev => [...prev, {
          type: 'bot',
          content: 'Lo siento, hubo un problema al procesar tu imagen.'
        }]);
      } finally {
        setIsTyping(false);
        // Limpiar estados
        setSelectedImage(null);
        setImagePreviewUrl('');
        // Restaurar el placeholder original
        const inputField = document.querySelector('.chat-input');
        if (inputField) {
          inputField.placeholder = "Enviar mensaje";
        }
      }
      return;
    }
    
    // Envío normal de texto (sin imagen)
    if (!inputMessage.trim()) return;

    const userMessage = {
        type: 'user',
        content: inputMessage.trim()
    };
    
    console.log('🟦 Usuario envía mensaje:', {
        mensaje: userMessage.content,
        chatId: chatId,
        timestamp: new Date().toISOString()
    });

    setMessages(prev => [...prev, userMessage]);
    setInputMessage('');
    setIsTyping(true);

    try {
        const data = await enviarTexto(userMessage.content, chatId);
        console.log('🟩 Respuesta del servidor:', {
            respuesta: data.respuesta,
            chatId: chatId,
            timestamp: new Date().toISOString()
        });

        const botResponse = {
            type: 'bot',
            content: data.respuesta || 'No hay respuesta'
        };
        setMessages(prev => [...prev, botResponse]);
    } catch (error) {
        console.error('🟥 Error en la comunicación:', {
            error: error.message,
            chatId: chatId,
            timestamp: new Date().toISOString()
        });
        
        setMessages(prev => [...prev, {
            type: 'bot',
            content: 'Lo siento, hubo un problema al procesar tu mensaje.'
        }]);
    } finally {
        setIsTyping(false);
    }
  };

  // Subir archivo (imagen) - modificado
  const handleFileUpload = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = async (e) => {
      const file = e.target.files[0];
      if (file) {
        console.log('🖼️ Imagen seleccionada:', {
          nombre: file.name,
          tipo: file.type,
          tamaño: `${(file.size / 1024).toFixed(2)}KB`,
        });

        // Crear URL para previsualización
        const fileUrl = URL.createObjectURL(file);
        setImagePreviewUrl(fileUrl);
        setSelectedImage(file);
        setShowImagePreview(true);
        
        // Enfocar el campo de entrada para que el usuario escriba la instrucción
        setTimeout(() => {
          const inputField = document.querySelector('.chat-input');
          if (inputField) {
            inputField.focus();
            inputField.placeholder = "Escribe una instrucción para la imagen...";
          }
        }, 100);
      }
    };
    input.click();
  };

  // Cancelar el envío de la imagen
  const handleCancelImageSend = () => {
    setShowImagePreview(false);
    setSelectedImage(null);
    setImagePreviewUrl('');
    
    // Restaurar el placeholder original
    const inputField = document.querySelector('.chat-input');
    if (inputField) {
      inputField.placeholder = "Enviar mensaje";
    }
  };

  // Capturar foto de la cámara - modificado
  const handleCameraCapture = async () => {
    try {
      // Verificar si la API de mediaDevices está disponible
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        // Si no está disponible, probablemente es porque estamos en HTTP en lugar de HTTPS
        console.error('Error: MediaDevices API no disponible. Las funciones de cámara requieren HTTPS.');
        alert('No se pudo acceder a la cámara. Esta función requiere una conexión segura (HTTPS) o usar localhost. Contacta al administrador del sistema.');
        return;
      }
      
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      const video = document.createElement('video');
      video.srcObject = stream;
      await video.play();

      const canvas = document.createElement('canvas');
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      canvas.getContext('2d').drawImage(video, 0, 0);

      // Convierte a Blob
      canvas.toBlob(async (blob) => {
        if (blob) {
          // Crear un nombre único para la imagen con timestamp
          const timestamp = new Date().getTime();
          const fileName = `camera_photo_${timestamp}.jpg`;
          
          // Crear un File object a partir del Blob con el nombre personalizado
          const imageFile = new File([blob], fileName, { type: 'image/jpeg' });
          
          const imageUrl = URL.createObjectURL(imageFile);
          
          // Mostrar la previsualización
          setImagePreviewUrl(imageUrl);
          setSelectedImage(imageFile);
          setShowImagePreview(true);
          
          console.log('📸 Foto capturada:', {
            nombre: fileName,
            tipo: imageFile.type,
            tamaño: `${(imageFile.size / 1024).toFixed(2)}KB`,
            timestamp: timestamp
          });
          
          // Enfocar el campo de entrada para que el usuario escriba la instrucción
          setTimeout(() => {
            const inputField = document.querySelector('.chat-input');
            if (inputField) {
              inputField.focus();
              inputField.placeholder = "Escribe una instrucción para la imagen...";
            }
          }, 100);
          
          // Detener la transmisión de video
          stream.getTracks().forEach(track => track.stop());
        }
      }, 'image/jpeg');
    } catch (error) {
      console.error('Error accessing camera:', error);
      
      // Mensajes específicos según el tipo de error
      if (error.name === 'NotAllowedError') {
        alert('No se pudo acceder a la cámara. Has denegado el permiso para acceder a la cámara.');
      } else if (error.name === 'NotFoundError') {
        alert('No se pudo acceder a la cámara. No se encontró ninguna cámara en tu dispositivo.');
      } else if (error.name === 'NotReadableError') {
        alert('No se pudo acceder a la cámara. La cámara está siendo utilizada por otra aplicación.');
      } else if (error.message && error.message.includes('getUserMedia')) {
        alert('No se pudo acceder a la cámara. Esta función requiere una conexión segura (HTTPS) o usar localhost.');
      } else {
        alert('No se pudo acceder a la cámara. Intenta recargar la página o utilizar otro navegador.');
      }
    }
  };

  // Grabar audio
  const handleVoiceRecording = async () => {
    if (!isRecording) {
      console.log('🎙️ Iniciando grabación de audio', {
        chatId: chatId,
        timestamp: new Date().toISOString()
      });
      // Iniciar grabación
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        const recorder = new MediaRecorder(stream);
        
        recorder.ondataavailable = (event) => {
          audioChunks.current.push(event.data);
        };

        recorder.onstop = async () => {
          const audioBlob = new Blob(audioChunks.current, { type: 'audio/wav' });
          audioChunks.current = [];

          const audioUrl = URL.createObjectURL(audioBlob);
          // Muestra el mensaje en la interfaz
          setMessages(prev => [...prev, {
            type: 'user',
            content: '🔊 Nota de voz',
            audio: audioUrl
          }]);
          setIsTyping(true);

          try {
            // Enviar al backend
            const data = await enviarAudio('Nota de voz', chatId, audioBlob);
            // Respuesta del bot
            const botResponse = {
              type: 'bot',
              content: data.respuesta || 'No hay respuesta'
            };
            setMessages(prev => [...prev, botResponse]);
          } catch (error) {
            console.error('Error al enviar audio:', error);
            setMessages(prev => [...prev, {
              type: 'bot',
              content: 'Lo siento, hubo un problema al procesar tu audio.'
            }]);
          } finally {
            setIsTyping(false);
          }
        };

        recorder.start();
        setMediaRecorder(recorder);
        setIsRecording(true);
      } catch (error) {
        console.error('Error accessing microphone:', error);
        alert('No se pudo acceder al micrófono');
      }
    } else {
      console.log('🎙️ Finalizando grabación de audio', {
        chatId: chatId,
        timestamp: new Date().toISOString()
      });
      // Detener grabación
      mediaRecorder.stop();
      setIsRecording(false);
      mediaRecorder.stream.getTracks().forEach(track => track.stop());
    }
  };

  // Renderiza un mensaje
  const renderMessageContent = (message) => {
    // Función para formatear el contenido con markdown simple
    const formatMessageContent = (content) => {
      if (!content) return '';
      
      // Patrones para detectar formatos de Markdown
      const patterns = [
        // Encabezados
        { regex: /^######\s+(.*?)$/gm, replacement: '<h6>$1</h6>' },
        { regex: /^#####\s+(.*?)$/gm, replacement: '<h5>$1</h5>' },
        { regex: /^####\s+(.*?)$/gm, replacement: '<h4>$1</h4>' },
        { regex: /^###\s+(.*?)$/gm, replacement: '<h3>$1</h3>' },
        { regex: /^##\s+(.*?)$/gm, replacement: '<h2>$1</h2>' },
        { regex: /^#\s+(.*?)$/gm, replacement: '<h1>$1</h1>' },
        
        // Estilos de texto
        { regex: /\*\*(.*?)\*\*/g, replacement: '<strong>$1</strong>' },
        { regex: /\*(.*?)\*/g, replacement: '<em>$1</em>' },
        
        // Bloques de código
        { regex: /```([\s\S]*?)```/g, replacement: '<pre><code>$1</code></pre>' },
        { regex: /`([^`]+)`/g, replacement: '<code>$1</code>' },
        
        // Listas numeradas y anidadas
        // Procesaremos las listas de manera especial después
        
        // Tablas - capturamos la tabla completa para procesarla después
        { 
          regex: /\|(.+)\|\n\|([-\s|]+)\|\n(\|.+\|\n)+/g, 
          replacement: (match) => formatTable(match) 
        },
      ];
      
      // Aplicar patrones básicos de formato
      let formattedContent = content;
      patterns.forEach(pattern => {
        formattedContent = formattedContent.replace(pattern.regex, pattern.replacement);
      });
      
      // Procesar listas numeradas y con viñetas (incluyendo anidación)
      formattedContent = processLists(formattedContent);
      
      return formattedContent;
    };
    
    // Función para procesar y formatear tablas
    const formatTable = (tableText) => {
      const lines = tableText.trim().split('\n');
      
      // Si no hay suficientes líneas para una tabla, devolver el texto original
      if (lines.length < 3) return tableText;
      
      // Extraer encabezados
      const headers = lines[0].split('|')
        .filter(cell => cell.trim() !== '')
        .map(cell => cell.trim());
      
      // Construir la tabla HTML
      let htmlTable = '<div class="md-table-container"><table class="md-table">';
      
      // Agregar encabezados
      htmlTable += '<thead><tr>';
      headers.forEach(header => {
        htmlTable += `<th>${header}</th>`;
      });
      htmlTable += '</tr></thead>';
      
      // Agregar filas de datos (saltamos la primera línea de encabezados y 
      // la segunda línea de separadores)
      htmlTable += '<tbody>';
      for (let i = 2; i < lines.length; i++) {
        const cells = lines[i].split('|')
          .filter(cell => cell.trim() !== '')
          .map(cell => cell.trim());
        
        htmlTable += '<tr>';
        cells.forEach(cell => {
          htmlTable += `<td>${cell}</td>`;
        });
        htmlTable += '</tr>';
      }
      
      htmlTable += '</tbody></table></div>';
      return htmlTable;
    };
    
    // Función para procesar listas numeradas y con viñetas
    const processLists = (content) => {
      // Separamos el contenido en líneas
      const lines = content.split('\n');
      
      // Detectar líneas que son parte de listas
      let inList = false;
      let listType = null; // 'ol' o 'ul'
      let listContent = '';
      let processedContent = [];
      let indentLevel = 0;
      let lastIndentLevel = 0;
      
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        
        // Detectar si la línea es parte de una lista numerada o con viñetas
        const numberedMatch = line.match(/^(\s*)(\d+)\.\s+(.*)$/);
        const bulletMatch = line.match(/^(\s*)[*-]\s+(.*)$/);
        
        if (numberedMatch || bulletMatch) {
          // Si no estábamos en una lista, iniciamos una nueva
          if (!inList) {
            inList = true;
            listType = numberedMatch ? 'ol' : 'ul';
            listContent = '';
          }
          
          // Determinar el nivel de indentación
          const spaces = numberedMatch ? numberedMatch[1].length : bulletMatch[1].length;
          indentLevel = Math.floor(spaces / 2); // Asume 2 espacios por nivel
          
          // Si hay cambio de indentación, manejarlo adecuadamente
          if (indentLevel > lastIndentLevel) {
            // Iniciar una sublista
            listContent += `<${numberedMatch ? 'ol' : 'ul'} class="nested-list">`;
          } else if (indentLevel < lastIndentLevel) {
            // Cerrar sublistas
            for (let j = 0; j < lastIndentLevel - indentLevel; j++) {
              listContent += `</${listType === 'ol' ? 'ol' : 'ul'}>`;
            }
          }
          
          // Añadir el ítem de la lista
          const content = numberedMatch ? numberedMatch[3] : bulletMatch[2];
          const number = numberedMatch ? numberedMatch[2] : null;
          
          if (numberedMatch) {
            listContent += `<li value="${number}" class="numbered-item">${content}</li>`;
          } else {
            listContent += `<li class="bullet-item">${content}</li>`;
          }
          
          lastIndentLevel = indentLevel;
        } else {
          // Si no es una línea de lista pero estábamos en una lista, la cerramos
          if (inList) {
            // Cerrar todas las sublistas pendientes
            for (let j = 0; j <= lastIndentLevel; j++) {
              listContent += `</${listType}>`;
            }
            
            // Añadir la lista al contenido procesado
            processedContent.push(`<div class="${listType === 'ol' ? 'numbered-list' : 'bullet-list'}-container">${listContent}</div>`);
            
            inList = false;
            listContent = '';
            indentLevel = 0;
            lastIndentLevel = 0;
          }
          
          // Añadir la línea normal al contenido procesado
          processedContent.push(line);
        }
      }
      
      // Si terminamos el contenido y aún estamos en una lista, la cerramos
      if (inList) {
        // Cerrar todas las sublistas pendientes
        for (let j = 0; j <= lastIndentLevel; j++) {
          listContent += `</${listType}>`;
        }
        
        // Añadir la lista al contenido procesado
        processedContent.push(`<div class="${listType === 'ol' ? 'numbered-list' : 'bullet-list'}-container">${listContent}</div>`);
      }
      
      return processedContent.join('\n');
    };

    return (
      <div className="message-content">
        <div dangerouslySetInnerHTML={{ __html: formatMessageContent(message.content) }} />
        {message.audio && (
          <div className="media-container">
            <audio controls src={message.audio} />
          </div>
        )}
        {message.image && (
          <div className="media-container">
            <img src={message.image} alt="Imagen adjunta" />
          </div>
        )}
      </div>
    );
  };

  useEffect(() => {
    console.log('📝 Estado actual de mensajes:', {
      cantidadMensajes: messages.length,
      mensajes: messages.map(m => ({
        tipo: m.type,
        contenido: m.content.substring(0, 50) + (m.content.length > 50 ? '...' : ''),
        tieneAudio: !!m.audio,
        tieneImagen: !!m.image
      })),
      chatId: chatId,
      timestamp: new Date().toISOString()
    });
  }, [messages]);

  const handleSelectChat = async (chatId) => {
    try {
        console.log('🔄 Cargando chat:', chatId);
        
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
        
        // Verificar si hay chats
        if (!data.chats || data.chats.length === 0) {
            console.log('ℹ️ No hay conversaciones disponibles');
            setMessages([{
                type: 'bot',
                content: 'No hay conversaciones disponibles. Puedes iniciar una nueva conversación.'
            }]);
            return;
        }
        
        // Convertir chatId a número para la comparación
        const numericChatId = parseInt(chatId, 10);
        const selectedChat = data.chats.find(chat => chat.id === numericChatId);

        if (!selectedChat) {
            console.log('❌ Chat no encontrado:', numericChatId);
            throw new Error('Chat no encontrado');
        }

        console.log('📱 Chat encontrado:', selectedChat);

        // Convertir los mensajes al formato que espera el componente
        const formattedMessages = selectedChat.messages.map(msg => {
            if (msg.role === 'user_media') {
                return {
                    type: 'user',
                    content: 'Imagen enviada',
                    image: msg.content
                };
            }
            return {
                type: msg.role === 'assistant' ? 'bot' : 'user',
                content: msg.content
            };
        });

        // Actualizar el estado
        setChatId(numericChatId);
        setMessages(formattedMessages);
        
        console.log('✅ Mensajes cargados:', formattedMessages.length);
    } catch (error) {
        console.error('❌ Error al cargar el chat:', error.message);
        setMessages([{
            type: 'bot',
            content: 'Lo siento, hubo un problema al cargar el chat.'
        }]);
    }
  };

  return (
    <div className="chat-screen-container">
      <div className={`chat-screen ${showImagePreview ? 'with-preview' : ''}`}>
        {/* Header */}
        <div className="chat-header">
          <div className="chat-back-wrapper">
            <button className="chat-back-button" onClick={onBack}>
              <svg xmlns="http://www.w3.org/2000/svg" width="17" height="15" viewBox="0 0 17 15" fill="none">
                <path d="M7.45408 13.6896L1.0805 7.47707L7.29305 1.10349M15.4646 7.29304L1.0805 7.47707L15.4646 7.29304Z" 
                      stroke="black" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            <img src={logoDashboard} alt="HealthIA" className="chat-logo" />
            <div className="chat-info">
              <span className="chat-name">HealthIA</span>
              <span className="chat-status">Active now</span>
            </div>
          </div>
          <button className="menu-options-button" onClick={() => setIsMenuOpen(true)}>
            <svg xmlns="http://www.w3.org/2000/svg" width="5" height="19" viewBox="0 0 5 19" fill="none">
              <path d="M2.5 5C1.25733 5 0.25 3.99264 0.25 2.75C0.25 1.50736 1.25733 0.5 2.5 0.5C3.74267 0.5 4.75 1.50736 4.75 2.75C4.75 3.99264 3.74267 5 2.5 5Z" />
              <path d="M2.5 11.75C1.25733 11.75 0.25 10.7427 0.25 9.5C0.25 8.25733 1.25733 7.25 2.5 7.25C3.74267 7.25 4.75 8.25733 4.75 9.5C4.75 10.7427 3.74267 11.75 2.5 11.75Z" />
              <path d="M2.5 18.5C1.25733 18.5 0.25 17.4927 0.25 16.25C0.25 15.0073 1.25733 14 2.5 14C3.74267 14 4.75 15.0073 4.75 16.25C4.75 17.4927 3.74267 18.5 2.5 18.5Z" />
            </svg>
          </button>
        </div>

        {/* Contenedor de mensajes */}
        <div className="messages-container">
          {messages.map((message, index) => (
            <div key={index} className={`message ${message.type}`}>
              {message.type === 'bot' && (
                <img
                  src={logoDashboard}
                  alt="HealthIA"
                  className="bot-avatar"
                />
              )}
              {renderMessageContent(message)}
            </div>
          ))}
          
          {/* Indicador "typing" */}
          {isTyping && (
            <div className="message bot">
              <img src={logoDashboard} alt="HealthIA" className="bot-avatar" />
              <div className="message-content typing">
                <span className="typing-dot"></span>
                <span className="typing-dot"></span>
                <span className="typing-dot"></span>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Previsualización de imagen integrada - Simplificada */}
        {showImagePreview && (
          <div className="image-preview-fixed">
            <div className="image-preview-header">
              <h3>Enviar imagen</h3>
              <button className="close-preview-button" onClick={handleCancelImageSend}>×</button>
            </div>
            <div className="image-preview-content">
              <div className="image-preview-img">
                <img src={imagePreviewUrl} alt="Vista previa" />
              </div>
            </div>
          </div>
        )}

        {/* Área de input */}
        <form onSubmit={handleSendMessage} className="chat-input-container">
          <img 
            src={clipIcon}
            alt="Attach"
            className="chat-action-icon"
            onClick={handleFileUpload}
          />
          <input
            type="text"
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            placeholder={showImagePreview ? "Escribe una instrucción para la imagen..." : "Enviar mensaje"}
            className="chat-input"
          />
          <img 
            src={cameraIcon}
            alt="Camera"
            className="chat-action-icon"
            onClick={handleCameraCapture}
          />
          <img 
            src={micIcon}
            alt="Voice"
            className={`chat-action-icon ${isRecording ? 'recording' : ''}`}
            onClick={handleVoiceRecording}
          />
          <button
            type="submit"
            className="send-button"
            disabled={!showImagePreview && !inputMessage.trim()}
          >
            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
          </button>
        </form>
      </div>

      <ChatOptionsMenu 
        isOpen={isMenuOpen} 
        onClose={() => setIsMenuOpen(false)}
        onNewChat={handleNewChat}
        onSelectChat={handleSelectChat}
      />
    </div>
  );
};

export default ChatScreen;
