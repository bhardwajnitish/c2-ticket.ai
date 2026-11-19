import { TicketDetailView } from "@/components/TicketDetailView";

type Props = {
  params: Promise<{ ticketId: string }>;
};

export default async function TicketDetailPage({ params }: Props) {
  const { ticketId } = await params;
  return <TicketDetailView ticketId={ticketId} />;
}