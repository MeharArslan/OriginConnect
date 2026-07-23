import { Module } from '@nestjs/common';
import { PresenceGateway } from './presence.gateway';
import { AuthModule } from '../auth/auth.module';
@Module({imports:[AuthModule],providers:[PresenceGateway],exports:[PresenceGateway]})
export class PresenceModule {}
