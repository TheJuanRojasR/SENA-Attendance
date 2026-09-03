import React from 'react';
import { Button, ButtonProps } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link } from 'react-router';

export interface ILinkButtonProps extends Omit<ButtonProps, 'as' | 'href'> {
  to: string;
  translationKey?: string;
  children?: string | React.ReactElement | (string | React.ReactElement)[];
  'data-cy'?: string;
}

const LinkButton = ({ to, translationKey, children, ...buttonProps }: ILinkButtonProps) => (
  <Button as={Link as any} to={to} {...buttonProps}>
    {translationKey ? <Translate contentKey={translationKey}>{children}</Translate> : children}
  </Button>
);

export default LinkButton;
