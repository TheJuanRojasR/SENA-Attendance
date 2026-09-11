import React from 'react';
import { Card } from 'react-bootstrap';

export interface IWelcomeBannerProps {
  firstName?: string;
}

export interface IDashboardCardsProps {}
// TODO: reemplazar el valor por defecto una vez exista el endpoint/reducer que trae el UserProfile del usuario autenticado.
export const WelcomeBanner = ({ firstName = 'Usuario' }: IWelcomeBannerProps) => (
  <div className="welcome-banner">
    <h2>Bienvenido de nuevo, {firstName}</h2>
  </div>
);

export const DashboardDescription = ({ children }: { children: React.ReactNode }) => <p> {children} </p>;
