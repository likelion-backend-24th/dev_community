import client from './client'

export async function openChat(questionId, content) {
  const res = await client.post(`/api/questions/${questionId}/chat-rooms`, { content })
  return res.data.data
}

export async function getMyChatRooms() {
  const res = await client.get('/api/chat-rooms')
  return res.data.data
}

export async function getChatRoom(roomId) {
  const res = await client.get(`/api/chat-rooms/${roomId}`)
  return res.data.data
}

export async function sendChatMessage(roomId, content) {
  const res = await client.post(`/api/chat-rooms/${roomId}/messages`, { content })
  return res.data.data
}

export async function acceptChat(roomId) {
  const res = await client.patch(`/api/chat-rooms/${roomId}/accept`)
  return res.data.data
}

export async function adoptChat(roomId) {
  const res = await client.patch(`/api/chat-rooms/${roomId}/adopt`)
  return res.data.data
}

export async function getUnreadChatRoomCount() {
  const res = await client.get('/api/chat-rooms/unread-count')
  return res.data.data
}

export async function markChatRoomRead(roomId) {
  await client.patch(`/api/chat-rooms/${roomId}/read`)
}
