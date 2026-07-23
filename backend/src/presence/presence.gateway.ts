import { WebSocketGateway, WebSocketServer, SubscribeMessage, OnGatewayConnection, OnGatewayDisconnect, ConnectedSocket, MessageBody } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { Injectable } from '@nestjs/common';
@Injectable()
@WebSocketGateway({ cors:{origin:'*'}, namespace:'/connect' })
export class PresenceGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer() server: Server;
  private userSockets=new Map<string,Set<string>>();
  private socketUsers=new Map<string,string>();
  constructor(private jwtService:JwtService,private config:ConfigService){}
  async handleConnection(client:Socket){
    try{
      const token=client.handshake.auth.token||client.handshake.headers.authorization?.split(' ')[1];
      const p=this.jwtService.verify(token,{secret:this.config.get('JWT_SECRET')});
      client.data.userId=p.sub;this.socketUsers.set(client.id,p.sub);
      if(!this.userSockets.has(p.sub))this.userSockets.set(p.sub,new Set());
      this.userSockets.get(p.sub).add(client.id);client.join('user:'+p.sub);
      this.server.emit('presence:'+p.sub,{userId:p.sub,isOnline:true});
    }catch(e){client.disconnect(true);}
  }
  async handleDisconnect(client:Socket){
    const userId=this.socketUsers.get(client.id);if(!userId)return;
    this.socketUsers.delete(client.id);const sockets=this.userSockets.get(userId);
    if(sockets){sockets.delete(client.id);if(sockets.size===0){this.userSockets.delete(userId);this.server.emit('presence:'+userId,{userId,isOnline:false,lastSeen:Date.now()});}}
  }
  @SubscribeMessage('join_conversation') join(@ConnectedSocket() c:Socket,@MessageBody() d:{conversationId:string}){c.join('conv:'+d.conversationId);}
  @SubscribeMessage('leave_conversation') leave(@ConnectedSocket() c:Socket,@MessageBody() d:{conversationId:string}){c.leave('conv:'+d.conversationId);}
  @SubscribeMessage('typing') typing(@ConnectedSocket() c:Socket,@MessageBody() d:{conversationId:string;isTyping:boolean}){c.to('conv:'+d.conversationId).emit('typing',{userId:c.data.userId,...d});}
  emitNewMessage(receiverId:string,msg:any){this.server.to('user:'+receiverId).emit('new_message',msg);}
  emitStatus(userId:string,data:any){this.server.to('user:'+userId).emit('message_status',data);}
  isOnline(userId:string){return(this.userSockets.get(userId)?.size??0)>0;}
}
