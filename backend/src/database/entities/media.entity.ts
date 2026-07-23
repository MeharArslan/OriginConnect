import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn } from 'typeorm';
@Entity('media')
export class Media {
  @PrimaryGeneratedColumn('uuid') id: string;
  @Column('uuid') uploaderId: string;
  @Column() filename: string;
  @Column() originalName: string;
  @Column() mimeType: string;
  @Column({ type: 'bigint' }) size: number;
  @Column() url: string;
  @Column({ nullable: true }) thumbnailUrl: string;
  @Column({ nullable: true }) width: number;
  @Column({ nullable: true }) height: number;
  @Column({ nullable: true }) duration: number;
  @CreateDateColumn() createdAt: Date;
}
