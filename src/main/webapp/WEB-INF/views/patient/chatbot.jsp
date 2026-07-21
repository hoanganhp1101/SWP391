<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
/* === Chatbot Floating UI === */
.chatbot-toggle {
    position: fixed;
    bottom: 2rem;
    right: 2rem;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 32px rgba(99, 102, 241, 0.4);
    z-index: 999;
    transition: transform 0.3s, box-shadow 0.3s;
    color: white;
    font-size: 1.5rem;
}

.chatbot-toggle:hover {
    transform: scale(1.1);
    box-shadow: 0 12px 40px rgba(99, 102, 241, 0.5);
}

.chatbot-toggle .badge-dot {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 12px;
    height: 12px;
    background: #22c55e;
    border-radius: 50%;
    border: 2px solid white;
    animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
    0%, 100% {
        transform: scale(1);
        opacity: 1;
    }
    50% {
        transform: scale(1.3);
        opacity: 0.7;
    }
}

.chatbot-window {
    position: fixed;
    bottom: 6.5rem;
    right: 2rem;
    width: 400px;
    max-height: 550px;
    background: rgba(15, 23, 42, 0.95);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(99, 102, 241, 0.25);
    border-radius: 20px;
    display: flex;
    flex-direction: column;
    z-index: 999;
    box-shadow: 0 25px 60px rgba(0, 0, 0, 0.5);
    transform: translateY(20px) scale(0.95);
    opacity: 0;
    pointer-events: none;
    transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
}

.chatbot-window.open {
    transform: translateY(0) scale(1);
    opacity: 1;
    pointer-events: auto;
}

.chatbot-header {
    padding: 1rem 1.25rem;
    background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.1));
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.chatbot-header-icon {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
    color: white;
}

.chatbot-header-info h4 {
    color: #f1f5f9;
    font-size: 0.875rem;
    font-weight: 600;
    margin: 0;
}

.chatbot-header-info span {
    color: #22c55e;
    font-size: 0.7rem;
}

.chatbot-close {
    margin-left: auto;
    background: none;
    border: none;
    color: #94a3b8;
    font-size: 1.25rem;
    cursor: pointer;
    transition: color 0.2s;
}

.chatbot-close:hover {
    color: #f1f5f9;
}

.chatbot-messages {
    flex: 1;
    overflow-y: auto;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    max-height: 380px;
}

.chat-msg {
    max-width: 85%;
    padding: 0.75rem 1rem;
    border-radius: 16px;
    font-size: 0.8125rem;
    line-height: 1.5;
    word-wrap: break-word;
    animation: fadeInMsg 0.3s ease;
}

@keyframes fadeInMsg {
    from {
        opacity: 0;
        transform: translateY(8px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.chat-msg.bot {
    align-self: flex-start;
    background: rgba(99, 102, 241, 0.12);
    color: #e2e8f0;
    border-bottom-left-radius: 4px;
}

.chat-msg.user {
    align-self: flex-end;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    color: white;
    border-bottom-right-radius: 4px;
}

.chat-typing {
    align-self: flex-start;
    display: flex;
    gap: 4px;
    padding: 0.75rem 1rem;
}

.chat-typing span {
    width: 8px;
    height: 8px;
    background: #6366f1;
    border-radius: 50%;
    animation: typing 1.4s infinite;
}

.chat-typing span:nth-child(2) {
    animation-delay: 0.2s;
}

.chat-typing span:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes typing {
    0%, 100% {
        opacity: 0.3;
        transform: scale(0.8);
    }
    50% {
        opacity: 1;
        transform: scale(1.1);
    }
}

.chatbot-input {
    display: flex;
    padding: 0.75rem 1rem;
    gap: 0.5rem;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
    background: rgba(255, 255, 255, 0.02);
}

.chatbot-input input {
    flex: 1;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 0.625rem 1rem;
    color: #e2e8f0;
    font-size: 0.8125rem;
    outline: none;
    transition: border-color 0.2s;
}

.chatbot-input input::placeholder {
    color: #64748b;
}

.chatbot-input input:focus {
    border-color: rgba(99, 102, 241, 0.5);
}

.chatbot-input button {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    border: none;
    border-radius: 12px;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    cursor: pointer;
    transition: transform 0.2s;
}

.chatbot-input button:hover {
    transform: scale(1.05);
}

.chatbot-input button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.chatbot-disclaimer {
    padding: 0.5rem 1rem;
    font-size: 0.65rem;
    color: #64748b;
    text-align: center;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
    line-height: 1.4;
}

.chat-msg.emergency {
    background: rgba(239, 68, 68, 0.15);
    border: 1px solid rgba(239, 68, 68, 0.35);
    color: #fecaca;
}

.chat-msg.blocked,
.chat-msg.out-of-scope {
    background: rgba(245, 158, 11, 0.12);
    border: 1px solid rgba(245, 158, 11, 0.3);
    color: #fde68a;
}
</style>

<!-- Chatbot Toggle Button -->
<button class="chatbot-toggle" id="chatbotToggle" title="Hỏi AI DiabCare">
    <i class="fas fa-robot"></i>
    <div class="badge-dot"></div>
</button>

<!-- Chatbot Window -->
<div class="chatbot-window" id="chatbotWindow">
    <div class="chatbot-header">
        <div class="chatbot-header-icon"><i class="fas fa-robot"></i></div>
        <div class="chatbot-header-info">
            <h4>DiabCare AI</h4>
            <span>● Đang hoạt động</span>
        </div>
        <button class="chatbot-close" id="chatbotClose"><i class="fas fa-times"></i></button>
    </div>
    <div class="chatbot-messages" id="chatbotMessages">
        <div class="chat-msg bot">Xin chào! Tôi là trợ lý AI DiabCare. Bạn có thể trò chuyện <strong>bất kỳ chủ đề nào</strong> — tôi sẽ trả lời và cùng bạn quay lại theo dõi <strong>bệnh án, tiểu đường và sức khỏe</strong>.</div>
        <div class="chat-msg bot">Tôi <strong>không</strong> chẩn đoán bệnh hay kê/đổi liều thuốc. Mọi quyết định điều trị cần được bác sĩ tư vấn.</div>
    </div>
    <div class="chatbot-disclaimer">Thông tin chỉ mang tính tham khảo — không thay thế ý kiến bác sĩ.</div>
    <div class="chatbot-input">
        <input type="text" id="chatInput" placeholder="Nhập câu hỏi..." autocomplete="off">
        <button id="chatSendBtn" title="Gửi"><i class="fas fa-paper-plane"></i></button>
    </div>
</div>

<script>
    // ==================== CHATBOT LOGIC ====================
    const chatToggle = document.getElementById('chatbotToggle');
    const chatWindow = document.getElementById('chatbotWindow');
    const chatClose = document.getElementById('chatbotClose');
    const chatInput = document.getElementById('chatInput');
    const chatSendBtn = document.getElementById('chatSendBtn');
    const chatMessages = document.getElementById('chatbotMessages');

    if (chatToggle) {
        chatToggle.addEventListener('click', () => {
            chatWindow.classList.toggle('open');
            if (chatWindow.classList.contains('open')) {
                chatInput.focus();
            }
        });
    }

    if (chatClose) {
        chatClose.addEventListener('click', () => {
            chatWindow.classList.remove('open');
        });
    }

    if (chatInput) {
        chatInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendChat();
            }
        });
    }

    if (chatSendBtn) {
        chatSendBtn.addEventListener('click', sendChat);
    }

    let chatSending = false;

    function sendChat() {
        const msg = chatInput.value.trim();
        if (!msg || chatSending) return;

        chatSending = true;
        addMessage(msg, 'user');
        chatInput.value = '';
        chatSendBtn.disabled = true;

        const typingEl = document.createElement('div');
        typingEl.className = 'chat-typing';
        typingEl.innerHTML = '<span></span><span></span><span></span>';
        chatMessages.appendChild(typingEl);
        chatMessages.scrollTop = chatMessages.scrollHeight;

        fetch('${pageContext.request.contextPath}/ai-chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            body: 'message=' + encodeURIComponent(msg)
        })
            .then(res => res.json())
            .then(data => {
                typingEl.remove();
                const status = data.status || 'answered';
                const botType = status === 'emergency' ? 'emergency'
                    : status === 'blocked' ? 'blocked'
                    : status === 'out_of_scope' ? 'out-of-scope'
                    : 'bot';
                addMessage(data.reply || 'Xin lỗi, tôi không thể trả lời lúc này.', botType);
            })
            .catch(err => {
                typingEl.remove();
                addMessage('Lỗi kết nối. Vui lòng thử lại sau.', 'bot');
                console.error('Chat error:', err);
            })
            .finally(() => {
                chatSending = false;
                chatSendBtn.disabled = false;
                chatInput.focus();
            });
    }

    function addMessage(text, type) {
        const msgEl = document.createElement('div');
        msgEl.className = 'chat-msg ' + type;
        msgEl.textContent = text;
        chatMessages.appendChild(msgEl);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
</script>
