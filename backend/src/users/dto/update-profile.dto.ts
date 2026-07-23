import { IsString, IsOptional, MaxLength, IsIn } from 'class-validator';
export class UpdateProfileDto {
  @IsOptional() @IsString() @MaxLength(100) displayName?: string;
  @IsOptional() @IsString() @MaxLength(500) about?: string;
  @IsOptional() @IsIn(['everyone','contacts','nobody']) lastSeenPrivacy?: string;
  @IsOptional() @IsIn(['everyone','contacts','nobody']) photoPrivacy?: string;
}
