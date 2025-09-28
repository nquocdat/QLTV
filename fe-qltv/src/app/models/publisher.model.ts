export interface Publisher {
  id: number;
  name: string;
  address?: string;
  phoneNumber?: string;
  email?: string;
  website?: string;
  createdDate?: Date;
  updatedDate?: Date;
}
